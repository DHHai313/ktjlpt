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
public class VerifyForgotOtpRequest {

    // Email tương ứng với OTP đã được gửi
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;

    // Mã OTP 6 số nhận được qua email
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    String otp;
}
