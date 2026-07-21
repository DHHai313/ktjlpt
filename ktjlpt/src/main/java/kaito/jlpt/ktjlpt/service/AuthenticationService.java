package kaito.jlpt.ktjlpt.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kaito.jlpt.ktjlpt.dto.request.ExchangeTokenRequest;
import kaito.jlpt.ktjlpt.dto.request.IntrospectRequest;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.entity.User;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.enums.Role;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.UserRepository;
import kaito.jlpt.ktjlpt.repository.httpclient.OutboundIdentityClient;
import kaito.jlpt.ktjlpt.repository.httpclient.OutboundUserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    OutboundUserClient outboundUserClient;
    OutboundIdentityClient outboundIdentityClient;
    UserRepository userRepository;
    RedisService redisService;

    static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${spring.jwt.access-exp}")
    protected long ACCESS_EXPIRATION;

    @NonFinal
    @Value("${spring.jwt.refresh-exp}")
    protected long REFRESH_EXPIRATION;

    @NonFinal
    @Value("${spring.outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${spring.outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${spring.outbound.identity.redirect_uri}")
    protected String REDIRECT_URI;

    @NonFinal
    @Value("${spring.outbound.identity.grant_type}")
    protected String GRANT_TYPE;

    // =====================================================================
    //  GOOGLE OAUTH2 — Đăng nhập / Đăng ký duy nhất
    // =====================================================================

    /**
     * Xử lý luồng Google OAuth2 Authorization Code.
     * - Đổi code lấy Google Access Token.
     * - Lấy thông tin user từ Google.
     * - Nếu email chưa có trong DB → tạo user mới (role = USER, provider = GOOGLE).
     * - Nếu email đã có → cập nhật lastLoginAt.
     * - Tạo Access Token (ngắn hạn) trả về trong JSON body.
     * - Tạo Refresh Token (dài hạn) set vào HttpOnly Cookie.
     */
    @Transactional
    public AuthenticationResponse outboundAuthentication(String code, HttpServletResponse response) {
        // 1. Đổi authorization code lấy Google token
        var googleTokenResponse = outboundIdentityClient.exchangeToken(
                ExchangeTokenRequest.builder()
                        .code(code)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URI)
                        .grantType(GRANT_TYPE)
                        .build()
        );

        // 2. Lấy thông tin user từ Google
        var userInfo = outboundUserClient.getUserInfo("json", googleTokenResponse.getAccessToken());
        log.info("Google OAuth2 - user info fetched: email={}, name={}", userInfo.getEmail(), userInfo.getName());

        // 3. Tìm hoặc tạo user trong DB
        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existingUser -> {
                    existingUser.setLastLoginAt(Instant.now());
                    // Cập nhật avatar nếu Google có ảnh mới
                    if (userInfo.getPicture() != null) {
                        existingUser.setAvatarUrl(userInfo.getPicture());
                    }
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    log.info("Google OAuth2 - new user, creating account for: {}", userInfo.getEmail());
                    return userRepository.save(
                            User.builder()
                                    .email(userInfo.getEmail())
                                    .userName(userInfo.getName())
                                    .avatarUrl(userInfo.getPicture())
                                    .role(Role.USER)
                                    .isActive(true)

                                    .lastLoginAt(Instant.now())
                                   
                                    .build()
                    );
                });

        // 4. Tạo JWT của hệ thống
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // 5. Lưu Refresh Token vào Redis Whitelist (theo JTI)
        saveRefreshTokenToRedis(refreshToken);

        // 6. Set Refresh Token vào HttpOnly Cookie
        setRefreshTokenCookie(response, refreshToken);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    // =====================================================================
    //  REFRESH TOKEN — Đọc từ HttpOnly Cookie
    // =====================================================================

    /**
     * Cấp lại Access Token mới và rotate Refresh Token.
     * - Đọc Refresh Token từ HttpOnly Cookie.
     * - Validate chữ ký + hạn + type + Redis whitelist.
     * - Xóa token cũ khỏi Redis.
     * - Tạo bộ token mới: Access Token (JSON body) + Refresh Token (cookie mới).
     */
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {

        // 1. Đọc Refresh Token từ Cookie
        String refreshToken = extractRefreshTokenFromCookie(request)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 2. Verify chữ ký
        SignedJWT signedJWT = SignedJWT.parse(refreshToken);
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        if (!signedJWT.verify(verifier)) {
            clearRefreshTokenCookie(response);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // 3. Kiểm tra hết hạn
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime.before(new Date())) {
            clearRefreshTokenCookie(response);
            throw new AppException(ErrorCode.EXPIRED_TOKEN);
        }

        // 4. Kiểm tra type claim
        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"refresh".equals(type)) {
            clearRefreshTokenCookie(response);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        String jti = signedJWT.getJWTClaimsSet().getJWTID();

        // 5. Kiểm tra Redis Whitelist
        String storedToken = redisService.getRefreshToken(jti);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            clearRefreshTokenCookie(response);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // 6. Xóa token cũ khỏi Redis (Token Rotation)
        redisService.deleteRefreshToken(jti);

        // 7. Tìm user theo email (subject)
        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 8. Tạo bộ token mới
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        saveRefreshTokenToRedis(newRefreshToken);
        setRefreshTokenCookie(response, newRefreshToken);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // =====================================================================
    //  LOGOUT — Blacklist Access Token + Clear Cookie + Remove from Redis
    // =====================================================================

    /**
     * Logout:
     * 1. Blacklist Access Token trong Redis (nếu còn hạn).
     * 2. Đọc Refresh Token từ Cookie, xóa khỏi Redis Whitelist.
     * 3. Clear HttpOnly Cookie.
     */
    public void logout(String accessToken, HttpServletRequest request, HttpServletResponse response)
            throws ParseException, JOSEException {

        // 1. Blacklist Access Token
        try {
            var accessJwt = verifyToken(accessToken);
            String jti = accessJwt.getJWTClaimsSet().getJWTID();
            Date expiryTime = accessJwt.getJWTClaimsSet().getExpirationTime();
            long remainTimeMs = expiryTime.getTime() - System.currentTimeMillis();
            if (remainTimeMs > 0) {
                redisService.blacklistAccessToken(jti, remainTimeMs);
            }
        } catch (AppException e) {
            log.info("Logout - Access token already expired or invalid, skipping blacklist");
        }

        // 2. Đọc Refresh Token từ Cookie và xóa khỏi Redis
        extractRefreshTokenFromCookie(request).ifPresent(refreshToken -> {
            try {
                SignedJWT refreshJwt = SignedJWT.parse(refreshToken);
                JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
                if (refreshJwt.verify(verifier)) {
                    String jti = refreshJwt.getJWTClaimsSet().getJWTID();
                    redisService.deleteRefreshToken(jti);
                    log.info("Logout - Refresh token removed from Redis: jti={}", jti);
                }
            } catch (Exception e) {
                log.info("Logout - Refresh token invalid or already removed from Redis");
            }
        });

        // 3. Clear Cookie
        clearRefreshTokenCookie(response);
    }

    // =====================================================================
    //  INTROSPECT
    // =====================================================================

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    // =====================================================================
    //  TOKEN GENERATION
    // =====================================================================

    /**
     * Tạo Access Token ngắn hạn (15 phút).
     * subject = user.email để phục vụ findByEmail() trong refreshToken().
     */
    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("ktjlpt.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(ACCESS_EXPIRATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "ROLE_" + user.getRole())
                .claim("type", "access")
                .claim("userId", user.getId())
                .build();
        return signJWT(header, claimsSet, "Access");
    }

    /**
     * Tạo Refresh Token dài hạn (7 ngày).
     */
    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("ktjlpt.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(REFRESH_EXPIRATION, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .build();
        return signJWT(header, claimsSet, "Refresh");
    }

    private String signJWT(JWSHeader header, JWTClaimsSet claimsSet, String tokenType) {
        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate {} Token", tokenType, e);
            throw new RuntimeException("Cannot generate " + tokenType + " Token", e);
        }
    }

    // =====================================================================
    //  TOKEN VERIFICATION
    // =====================================================================

    /**
     * Verify Access Token: chữ ký, hạn, type="access", không bị blacklist trong Redis.
     */
    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        boolean verified = signedJWT.verify(verifier);
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");

        if (!"access".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        // Kiểm tra Blacklist Redis
        if (redisService.isBlacklisted(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    // =====================================================================
    //  COOKIE HELPERS
    // =====================================================================

    /**
     * Set Refresh Token vào HttpOnly Cookie (Secure=false cho dev local).
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)          // Đặt true khi deploy production với HTTPS
                .path("/ktjlpt/auth")   // Chỉ gửi cookie cho các request đến /ktjlpt/auth
                .maxAge(Duration.ofSeconds(REFRESH_EXPIRATION))
                .sameSite("Lax")        // Lax phù hợp cho redirect OAuth2
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Xóa Refresh Token Cookie (đặt Max-Age=0).
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/ktjlpt/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Đọc Refresh Token từ Cookie trong request.
     */
    private Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cookie.getValue());
    }

    // =====================================================================
    //  REDIS HELPERS
    // =====================================================================

    private void saveRefreshTokenToRedis(String refreshToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(refreshToken);
            String jti = jwt.getJWTClaimsSet().getJWTID();
            redisService.saveRefreshToken(jti, refreshToken, REFRESH_EXPIRATION);
            log.debug("Refresh token saved to Redis: jti={}", jti);
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}
