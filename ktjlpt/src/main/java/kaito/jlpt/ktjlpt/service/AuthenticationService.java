package kaito.jlpt.ktjlpt.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import kaito.jlpt.ktjlpt.dto.request.AuthenticationRequest;
import kaito.jlpt.ktjlpt.dto.request.IntrospectRequest;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
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

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected  String SIGNER_KEY;

    PasswordEncoder passwordEncoder;

    public IntrospectResponse introspect(IntrospectRequest introspectRequest)
            throws JOSEException, ParseException
    {
        var token = introspectRequest.getToken();

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirtyTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        var vertified = signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(vertified && expirtyTime.after(new Date()))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        var token = generateToken(user.getUsername());
        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    private String generateToken(String username){
        //header jwt
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        //payload - data->claims
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(username)//dai kien cho claims dang nhap
                .issuer("ktjlpt.com")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .claim("role", "custom")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        //JWS
        JWSObject jwsObject = new JWSObject(header,payload);
        //sign
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return  jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot generate JWS Token",e);
            throw new RuntimeException(e);
        }
    }
}
