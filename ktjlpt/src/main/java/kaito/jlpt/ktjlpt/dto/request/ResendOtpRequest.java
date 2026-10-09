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
public class ResendOtpRequest {
    // Email cần gửi lại OTP
    @NotBlank
    @Email
    String email;
}
