package kaito.jlpt.ktjlpt.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kaito.jlpt.ktjlpt.dto.request.IntrospectRequest;
import kaito.jlpt.ktjlpt.dto.request.LogoutRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.AuthenticationResponse;
import kaito.jlpt.ktjlpt.dto.response.IntrospectResponse;
import kaito.jlpt.ktjlpt.service.AuthenticationService;
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

    /**
     * Google OAuth2 Callback.
     * Frontend redirect user đến Google, Google redirect về frontend kèm ?code=...
     * Frontend gọi endpoint này để đổi code lấy token.
     *
     * Response:
     * - Body JSON: { "accessToken": "..." }
     * - Cookie: refresh_token=...; HttpOnly; SameSite=Lax; Path=/ktjlpt/auth
     */
    @PostMapping("/outbound/authentication")
    public ApiResponse<AuthenticationResponse> outboundAuthentication(
            @RequestParam("code") String code,
            HttpServletResponse response) {
        var result = authenticationService.outboundAuthentication(code, response);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    /**
     * Refresh Token endpoint.
     * Đọc Refresh Token từ HttpOnly Cookie, trả về Access Token mới.
     * Cookie refresh_token cũng được cập nhật (Token Rotation).
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request, response);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Logout endpoint.
     * - Access Token gửi trong body JSON để blacklist vào Redis.
     * - Refresh Token được đọc từ HttpOnly Cookie, xóa khỏi Redis.
     * - Cookie được clear.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws ParseException, JOSEException {
        authenticationService.logout(request.getAccessToken(), httpRequest, httpResponse);
        return ApiResponse.<Void>builder()
                .message("Logged out successfully")
                .build();
    }

    /**
     * Introspect endpoint — kiểm tra token còn hợp lệ không.
     */
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(
            @RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
}
