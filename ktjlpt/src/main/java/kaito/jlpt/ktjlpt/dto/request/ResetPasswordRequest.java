package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPasswordRequest {

    // Email của tài khoản cần đặt lại mật khẩu
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    // Reset token nhận được từ bước verify OTP (step 2)
    @NotBlank(message = "Reset token is required")
    String resetToken;

    // Mật khẩu mới — tối thiểu 6 ký tự
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword;

    // Xác nhận lại mật khẩu mới — phải khớp với newPassword
    @NotBlank(message = "Confirm password is required")
    String confirmPassword;
}
