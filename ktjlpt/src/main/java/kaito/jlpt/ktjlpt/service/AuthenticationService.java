package kaito.jlpt.ktjlpt.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import kaito.jlpt.ktjlpt.dto.request.AuthenticationRequest;
import kaito.jlpt.ktjlpt.dto.request.IntrospectRequest;
import kaito.jlpt.ktjlpt.dto.request.LogoutRequest;
import kaito.jlpt.ktjlpt.dto.request.RefreshRequest;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.entity.User;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.mapper.UserMapper;
import kaito.jlpt.ktjlpt.repository.RoleRepository;
import kaito.jlpt.ktjlpt.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    UserMapper userMapper;

    RedisService redisService;


    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected  String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.access-exp}")
    protected long ACCESS_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh-exp}")
    protected long REFRESH_EXPIRATION;


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
//    public IntrospectResponse introspect(IntrospectRequest introspectRequest)
//            throws JOSEException, ParseException
//    {
//        var token = introspectRequest.getToken();
//
//        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
//
//        SignedJWT signedJWT = SignedJWT.parse(token);
//
//        Date expirtyTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//        var vertified = signedJWT.verify(verifier);
//
//        return IntrospectResponse.builder()
//                .valid(vertified && expirtyTime.after(new Date()))
//                .build();
//    }
//    @Transactional
//    public AuthenticationResponse authenticate(AuthenticationRequest request){
//        var user = userRepository.findByUsername(request.getUsername())
//                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
//        if(!authenticated){
//            throw new AppException(ErrorCode.UNAUTHENTICATED);
//        }
//        var token = generateToken(user);
//        user.setLastLoginAt(Instant.now());
//
//        return AuthenticationResponse.builder()
//                .token(token)
//                .authenticated(true)
//                .build();
//    }
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var accessToken = generateToken(user, ACCESS_EXPIRATION);
        var refreshToken = generateRefreshToken(user, REFRESH_EXPIRATION);

        // THÊM: Lưu Refresh Token vào Redis (tự động hủy theo thời gian REFRESH_EXPIRATION)
        redisService.saveRefreshToken(user.getUsername(), refreshToken, REFRESH_EXPIRATION);

        return AuthenticationResponse.builder()
                .user(userMapper.toUserResponse(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
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
            if (refreshJwt.verify(verifier)) {
                String username = refreshJwt.getJWTClaimsSet().getSubject();
                redisService.deleteRefreshToken(username);
            }
        } catch (Exception e) {
            log.info("Refresh token invalid or already removed");
        }
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

        // 2. KIỂM TRA TRONG REDIS: Token có tồn tại và khớp với Redis Whitelist không?
        String storedToken = redisService.getRefreshToken(username);
        if (storedToken == null || !storedToken.equals(request.getToken())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Bị xóa khỏi Redis rồi hoặc không khớp
        }

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 3. TOKEN ROTATION: Tạo bộ token mới
        var newAccessToken = generateToken(user, ACCESS_EXPIRATION);
        var newRefreshToken = generateRefreshToken(user, REFRESH_EXPIRATION);

        // Lưu Refresh Token mới (ghi đè cái cũ)
        redisService.saveRefreshToken(username, newRefreshToken, REFRESH_EXPIRATION);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }
    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // THAY ĐỔI: Check Blacklist trên Redis thay vì SQL Database
        if (redisService.isBlacklisted(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }
    private String generateToken(User user,long validDuration){
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
        JWSObject jwsObject = new JWSObject(header,payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return  jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate Access Token",e);
            throw new RuntimeException(e);
        }
    }
    private String generateRefreshToken(User user,long validDuration){
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
        JWSObject jwsObject = new JWSObject(header,payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return  jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate Refresh Token",e);
            throw new RuntimeException(e);
        }
    }
    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                role.getPermissions().forEach(permission -> {stringJoiner.add(permission.getName());});
            });

        }
        return stringJoiner.toString();
    }
}
