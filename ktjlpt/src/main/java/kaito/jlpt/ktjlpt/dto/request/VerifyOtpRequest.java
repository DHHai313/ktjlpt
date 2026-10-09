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
public class VerifyOtpRequest {
    @NotBlank
    @Email
    String email;

    // OTP phải đúng 6 ký tự số
    @NotBlank
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    String otp;
}
