package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
    @NotBlank
    @Size(min = 6, max = 30, message = "PASSWORD_INVALID")
    String oldPassword;
    @NotBlank
    @Size(min = 6, max = 30, message = "PASSWORD_INVALID")
    String newPassword;
    @NotBlank
    @Size(min = 6, max = 30, message = "PASSWORD_INVALID")
    String confirmPassword;
}
