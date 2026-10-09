package kaito.jlpt.ktjlpt.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.*;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.service.AuthenticationService;
import kaito.jlpt.ktjlpt.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;


    @PostMapping(value = "/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/outbound/authentication")
    public ApiResponse<AuthenticationResponse> outboundAuthentication(@RequestParam("code") String code) {
        var result = authenticationService.outboundAuthentication(code);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping(value = "/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .message("User logged out successfully")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully")
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request)
            throws ParseException, JOSEException {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder().message("Change password successfully").build();
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        authenticationService.verifyEmail(request);
        return ApiResponse.<Void>builder().message("Verify email successfully").build();
    }

    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authenticationService.resendOtp(request);
        return ApiResponse.<Void>builder().message("Resend otp successfully").build();
    }
}
