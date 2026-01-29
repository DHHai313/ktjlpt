package kaito.jlpt.ktjlpt.enums;


public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized exception"),
    USER_EXISTED(1000,"User already exists"),
    INVALID_KEY(1001,"Invalid key"),
    EMAIL_EXISTED(1002,"Email already exists"),
    USERNAME_INVALID(1003,"Username must be at least 3 characters"),
    PASSWORD_INVALID(1004,"Password must be at least 6 characters"),
    USER_NOT_FOUND(1005,"User not found"),
    UNAUTHENTICATED(1006,"Unauthenticated"),

    ;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
