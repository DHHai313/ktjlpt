package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordRequest {

    // Email của tài khoản cần đặt lại mật khẩu
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;
}
