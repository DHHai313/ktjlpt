package kaito.jlpt.ktjlpt.enums;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1000, "User already exists", HttpStatus.BAD_REQUEST),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1002, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You don't have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008, "Invalid token", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(1009, "Token Expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_NOT_MATCH(1010, "Password Do Not Match", HttpStatus.UNAUTHORIZED),
    INCORRECT_PASSWORD(1011, "OldPassword incorret ", HttpStatus.UNAUTHORIZED),

    // === Email OTP Verification ===
    EMAIL_NOT_VERIFIED(1012, "Email has not been verified", HttpStatus.FORBIDDEN),
    INVALID_OTP(1013, "Invalid or expired OTP", HttpStatus.BAD_REQUEST),
    OTP_COOLDOWN(1014, "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_ALREADY_VERIFIED(1015, "Email is already verified", HttpStatus.BAD_REQUEST),

    // === Forgot Password ===
    RESET_TOKEN_INVALID(1016, "Reset token is invalid or expired", HttpStatus.BAD_REQUEST),
    RESET_OTP_COOLDOWN(1017, "Please wait before requesting a new password reset OTP", HttpStatus.TOO_MANY_REQUESTS),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
