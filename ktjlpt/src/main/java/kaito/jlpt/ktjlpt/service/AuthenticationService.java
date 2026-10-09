package kaito.jlpt.ktjlpt.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import kaito.jlpt.ktjlpt.dto.request.*;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.ForgotPasswordTokenResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.entity.User;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.enums.Provider;
import kaito.jlpt.ktjlpt.enums.Role;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.mapper.UserMapper;
import kaito.jlpt.ktjlpt.repository.httpclient.OutboundIdentityClient;
import kaito.jlpt.ktjlpt.repository.UserRepository;
import kaito.jlpt.ktjlpt.repository.httpclient.OutboundUserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    OutboundUserClient outboundUserClient;
    OutboundIdentityClient outboundIdentityClient;
    UserRepository userRepository;
    UserMapper userMapper;
    RedisService redisService;
    PasswordEncoder passwordEncoder;

    // Service gửi email OTP qua Brevo
    EmailService emailService;

    // Thời gian hết hạn OTP (phút), đọc từ application.yaml
    @NonFinal
    @Value("${app.verification.otp-expiry-minutes}")
    long otpExpiryMinutes;
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

    @Transactional
    public AuthenticationResponse outboundAuthentication(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest
                .builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        //Onboard User Google
        var userInfor = outboundUserClient.getUserInfo("json", response.getAccessToken());
        log.info("Outbound Authentication Response: {}", response);
        log.info("User Infor: {}", userInfor);
//        // save user
//        Role userRole = roleRepository.findByName(kaito.jlpt.ktjlpt.enums.Role.USER.name())
//                .orElseThrow(() -> new RuntimeException("Role USER not found"));
//
//        Set<Role> roles = new HashSet<>();
//        roles.add(userRole);
        //save if new user
        var user = userRepository.findByEmail(userInfor.getEmail())
                .map(existingUser -> {
                    existingUser.setLastLoginAt(Instant.now());
                    return userRepository.save(existingUser);
                })
                .orElseGet(
                        () -> userRepository.save(User.builder()
                                .username(userInfor.getName())
                                .email(userInfor.getEmail())
                                .active(userInfor.getActive())
                                .avatarUrl(userInfor.getAvatarUrl())
                                .provider(Provider.GOOGLE)
                                .lastLoginAt(Instant.now())
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role(Role.USER)
                                .build()));
        return AuthenticationResponse.builder()
                .accessToken(response.getAccessToken())
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Chặn login nếu email chưa xác thực (chỉ áp dụng cho user LOCAL,
        // user Google OAuth không cần vì email đã được Google verify sẵn)
        if (Provider.LOCAL.equals(user.getProvider())
                && !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var accessToken = generateToken(user, ACCESS_EXPIRATION);
        var refreshToken = generateRefreshToken(user, REFRESH_EXPIRATION);

        // THÊM: Lưu Refresh Token vào Redis (tự động hủy theo thời gian REFRESH_EXPIRATION)
        //redisService.saveRefreshToken(user.getUsername(), refreshToken, REFRESH_EXPIRATION);
        try {
            SignedJWT refreshJwt = SignedJWT.parse(refreshToken);
            String refreshJti = refreshJwt.getJWTClaimsSet().getJWTID();

            // Lưu refresh token theo jti
            redisService.saveRefreshToken(refreshJti, refreshToken, REFRESH_EXPIRATION);
        } catch (ParseException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // 1. Xử lý Access Token: Kiểm tra hạn, nếu còn thì ném vào Blacklist (Redis)
        try {
            var accessJwt = verifyToken(request.getAccessToken());
            String jit = accessJwt.getJWTClaimsSet().getJWTID();
            Date expiryTime = accessJwt.getJWTClaimsSet().getExpirationTime();

            long remainTimeMs = expiryTime.getTime() - System.currentTimeMillis();
            if (remainTimeMs > 0) {
                redisService.blacklistAccessToken(jit, remainTimeMs);
            }
        } catch (AppException e) {
            log.info("Access token already expired or invalid");
        }

        // 2. Xử lý Refresh Token: Verify chữ ký và xóa khỏi Whitelist (Redis)
        try {
            SignedJWT refreshJwt = SignedJWT.parse(request.getRefreshToken());
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

            if (!refreshJwt.verify(verifier)) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            Date exp = refreshJwt.getJWTClaimsSet().getExpirationTime();
            if (exp.before(new Date())) {
                throw new AppException(ErrorCode.EXPIRED_TOKEN);
            }

            String jti = refreshJwt.getJWTClaimsSet().getJWTID();
            redisService.deleteRefreshToken(jti);

        } catch (Exception e) {
            log.info("Refresh token invalid or expired");
        }
//        try {
//            SignedJWT refreshJwt = SignedJWT.parse(request.getRefreshToken());
//            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//            if (refreshJwt.verify(verifier)) {
//                String username = refreshJwt.getJWTClaimsSet().getSubject();
//                redisService.deleteRefreshToken(username);
//            }
//        } catch (Exception e) {
//            log.info("Refresh token invalid or already removed");
//        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        // 1. Verify chữ ký thủ công (không gọi qua verifyToken vì verifyToken giờ check Blacklist của Access Token)
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        if (!signedJWT.verify(verifier)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra Token hết hạn chưa
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expiryTime.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"refresh".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var username = signedJWT.getJWTClaimsSet().getSubject();
        String jti = signedJWT.getJWTClaimsSet().getJWTID();

        // 2. KIỂM TRA TRONG REDIS: Token có tồn tại và khớp với Redis Whitelist không?
        String storedToken = redisService.getRefreshToken(jti);
        if (storedToken == null || !storedToken.equals(request.getToken())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Bị xóa khỏi Redis rồi hoặc không khớp
        }
        // ===== QUAN TRỌNG: XÓA TOKEN CŨ =====
        redisService.deleteRefreshToken(jti);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 3. TOKEN ROTATION: Tạo bộ token mới
        var newAccessToken = generateToken(user, ACCESS_EXPIRATION);
        var newRefreshToken = generateRefreshToken(user, REFRESH_EXPIRATION);
        // ===== LƯU BẰNG JTI MỚI =====
        String newJti = SignedJWT.parse(newRefreshToken)
                .getJWTClaimsSet()
                .getJWTID();
        // Lưu Refresh Token mới (ghi đè cái cũ)
        redisService.saveRefreshToken(newJti, newRefreshToken, REFRESH_EXPIRATION);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        var type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"access".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // THAY ĐỔI: Check Blacklist trên Redis thay vì SQL Database
        if (redisService.isBlacklisted(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    private String generateToken(User user, long validDuration) {
        //header jwt
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //payload - data->claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())//dai dien cho claims dang nhap
                .issuer("ktjlpt.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("type", "access")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        //JWS
        JWSObject jwsObject = new JWSObject(header, payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate Access Token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(User user, long validDuration) {
        //header jwt
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //payload - data->claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())//dai dien cho claims dang nhap
                .issuer("ktjlpt.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("type", "refresh")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        //JWS
        JWSObject jwsObject = new JWSObject(header, payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate Refresh Token", e);
            throw new RuntimeException(e);
        }
    }

    //    private String buildScope(User user) {
//        StringJoiner stringJoiner = new StringJoiner(" ");
//        if (!CollectionUtils.isEmpty(user.getRoles())) {
//            user.getRoles().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> {
//                        stringJoiner.add(permission.getName());
//                    });
//            });
//
//        }
//        return stringJoiner.toString();
//    }
    private String buildScope(User user) {
        if (user.getRole() == null) {
            return "";
        }

        return "ROLE_" + user.getRole().name();
    }

    public void changePassword(ChangePasswordRequest request) {
        if (!request.getConfirmPassword().equals(request.getNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    // ======================== Email OTP Verification ========================

    /**
     * Xác thực email bằng mã OTP 6 số.
     *
     * Flow:
     * 1. Tìm user theo email
     * 2. Kiểm tra email đã verify chưa (tránh verify lại)
     * 3. Lấy OTP từ Redis và so sánh với OTP user nhập
     * 4. Nếu đúng → cập nhật emailVerified = true, xoá OTP khỏi Redis
     *
     * @param request Chứa email và mã OTP 6 số
     */
    public void verifyEmail(VerifyOtpRequest request) {
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Kiểm tra email đã được verify trước đó chưa
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // 3. Lấy OTP từ Redis — null nghĩa là OTP hết hạn hoặc chưa gửi
        String storedOtp = redisService.getOtp(request.getEmail());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // 4. OTP đúng → cập nhật trạng thái email đã xác thực
        user.setEmailVerified(true);
        userRepository.save(user);

        // 5. Xoá OTP khỏi Redis (không cho dùng lại OTP cũ)
        redisService.deleteOtp(request.getEmail());
    }

    /**
     * Gửi lại mã OTP mới đến email.
     *
     * Flow:
     * 1. Tìm user theo email
     * 2. Kiểm tra email chưa verify (đã verify thì không cần OTP)
     * 3. Kiểm tra cooldown 60s (chống spam gửi liên tục)
     * 4. Sinh OTP mới → lưu Redis → set cooldown → gửi email
     *
     * @param request Chứa email cần gửi lại OTP
     */
    /**
     * Gửi lại mã OTP mới đến email.
     *
     * Flow:
     * 1. Tìm user theo email
     * 2. Kiểm tra email chưa verify (đã verify thì không cần OTP)
     * 3. Kiểm tra cooldown 60s (chống spam gửi liên tục)
     * 4. Sinh OTP mới → lưu Redis → set cooldown → gửi email
     *
     * @param request Chứa email cần gửi lại OTP
     */
    public void resendOtp(ResendOtpRequest request) {
        // 1. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Đã verify rồi thì không cần gửi OTP nữa
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        // 3. Kiểm tra cooldown — mỗi lần gửi phải cách nhau ít nhất 60 giây
        if (redisService.isCooldownActive(request.getEmail())) {
            throw new AppException(ErrorCode.OTP_COOLDOWN);
        }

        // 4. Sinh OTP mới 6 số
        String otp = emailService.generateOtp();

        // 5. Lưu OTP mới vào Redis (ghi đè OTP cũ nếu còn)
        redisService.saveOtp(request.getEmail(), otp, otpExpiryMinutes);

        // 6. Đặt cooldown 60 giây — chống spam resend
        redisService.setCooldown(request.getEmail(), 60);

        // 7. Gửi email OTP bất đồng bộ qua Brevo SMTP
        emailService.sendOtpEmail(request.getEmail(), otp);
    }

    // ======================== Forgot Password ========================

    /**
     * Bước 1 — Yêu cầu đặt lại mật khẩu: gửi OTP đến email.
     *
     * Flow:
     * 1. Tìm user theo email — nếu không tồn tại vẫn trả 200 (bảo mật: không lộ email tồn tại)
     * 2. Kiểm tra cooldown 60s (chống spam)
     * 3. Sinh OTP 6 số → lưu Redis key "otp:reset:{email}" TTL 5 phút → set cooldown → gửi email
     *
     * @param request Chứa email cần đặt lại mật khẩu
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. Tìm user — nếu không tồn tại: im lặng (không throw) để bảo mật
        var optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            // Trả về bình thường, không báo lộ "email không tồn tại"
            return;
        }

        // 2. Kiểm tra cooldown — chống spam liên tục
        if (redisService.isResetOtpCooldownActive(request.getEmail())) {
            throw new AppException(ErrorCode.RESET_OTP_COOLDOWN);
        }

        // 3. Sinh OTP 6 số
        String otp = emailService.generateOtp();

        // 4. Lưu OTP vào Redis với TTL = otpExpiryMinutes (key riêng biệt với verify email)
        redisService.saveResetOtp(request.getEmail(), otp, otpExpiryMinutes);

        // 5. Đặt cooldown 60 giây
        redisService.setResetOtpCooldown(request.getEmail(), 60);

        // 6. Gửi email OTP quên mật khẩu bất đồng bộ qua Brevo
        emailService.sendForgotPasswordOtpEmail(request.getEmail(), otp);
    }

    /**
     * Bước 2 — Xác minh OTP quên mật khẩu.
     *
     * Flow:
     * 1. Lấy OTP từ Redis "otp:reset:{email}" — null = hết hạn / chưa gửi
     * 2. So sánh OTP → sai thì throw INVALID_OTP
     * 3. OTP đúng → xoá OTP khỏi Redis (không tái sử dụng)
     * 4. Sinh reset token (UUID) → lưu Redis "pwd:reset:{email}" TTL 10 phút
     * 5. Trả reset token về FE để dùng ở bước 3
     *
     * @param request Chứa email và mã OTP 6 số
     * @return ForgotPasswordTokenResponse chứa resetToken
     */
    public ForgotPasswordTokenResponse verifyForgotOtp(VerifyForgotOtpRequest request) {
        // 1. Lấy OTP từ Redis — null nghĩa là hết hạn hoặc chưa gửi
        String storedOtp = redisService.getResetOtp(request.getEmail());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // 2. OTP đúng → xoá khỏi Redis (one-time use)
        redisService.deleteResetOtp(request.getEmail());

        // 3. Sinh reset token ngẫu nhiên (UUID) — TTL 10 phút (600 giây)
        String resetToken = UUID.randomUUID().toString();
        redisService.saveResetToken(request.getEmail(), resetToken, 600);

        // 4. Trả reset token về FE
        return ForgotPasswordTokenResponse.builder()
                .resetToken(resetToken)
                .build();
    }

    /**
     * Bước 3 — Đặt mật khẩu mới sau khi OTP đã được xác minh.
     *
     * Flow:
     * 1. Kiểm tra newPassword == confirmPassword
     * 2. Lấy reset token từ Redis "pwd:reset:{email}" — null = hết hạn / chưa verify OTP
     * 3. So sánh token — sai thì throw RESET_TOKEN_INVALID
     * 4. Token đúng → encode & lưu mật khẩu mới → xoá reset token khỏi Redis
     *
     * @param request Chứa email, resetToken, newPassword, confirmPassword
     */
    public void resetPassword(ResetPasswordRequest request) {
        // 1. Kiểm tra confirm password khớp
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // 2. Lấy reset token từ Redis — null = hết hạn hoặc chưa verify OTP
        String storedToken = redisService.getResetToken(request.getEmail());
        if (storedToken == null || !storedToken.equals(request.getResetToken())) {
            throw new AppException(ErrorCode.RESET_TOKEN_INVALID);
        }

        // 3. Tìm user theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 4. Encode và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 5. Xoá reset token khỏi Redis — không cho dùng lại
        redisService.deleteResetToken(request.getEmail());
    }
}
