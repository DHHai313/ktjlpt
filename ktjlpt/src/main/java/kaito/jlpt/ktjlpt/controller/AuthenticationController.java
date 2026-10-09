package kaito.jlpt.ktjlpt.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.*;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.ForgotPasswordTokenResponse;
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

    // ======================== Forgot Password ========================

    /**
     * Bước 1: Gửi OTP đặt lại mật khẩu đến email.
     * Luôn trả 200 dù email không tồn tại (bảo mật: không lộ email).
     */
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ApiResponse.<Void>builder()
                .message("If your email is registered, you will receive a password reset OTP shortly")
                .build();
    }

    /**
     * Bước 2: Xác minh OTP — trả về resetToken để dùng ở bước 3.
     */
    @PostMapping("/verify-forgot-otp")
    public ApiResponse<ForgotPasswordTokenResponse> verifyForgotOtp(
            @Valid @RequestBody VerifyForgotOtpRequest request) {
        ForgotPasswordTokenResponse result = authenticationService.verifyForgotOtp(request);
        return ApiResponse.<ForgotPasswordTokenResponse>builder()
                .result(result)
                .message("OTP verified. Use the reset token to set your new password.")
                .build();
    }

    /**
     * Bước 3: Đặt mật khẩu mới bằng resetToken đã nhận ở bước 2.
     */
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password has been reset successfully")
                .build();
    }
}
