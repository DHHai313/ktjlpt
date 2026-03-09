package kaito.jlpt.ktjlpt.enums;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized exception",HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1000,"User already exists",HttpStatus.BAD_REQUEST),
    INVALID_KEY(1001,"Invalid key",HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1002,"Email already exists",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003,"Username must be at least 3 characters",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"Password must be at least 6 characters",HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1005,"User not found",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006,"Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007,"You don't have permission",HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008,"Invalid token",HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(1009,"Token Expired",HttpStatus.UNAUTHORIZED),


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
