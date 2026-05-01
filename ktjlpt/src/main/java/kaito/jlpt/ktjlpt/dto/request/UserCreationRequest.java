package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank
    @Email
    String email;

    @NotBlank
    @Size(min = 3, max = 30, message = "USERNAME_INVALID")
    String username;

    @NotBlank
    @Size(min = 5, max = 30, message = "PASSWORD_INVALID")
    String password;

    @Size(max = 5)
    @Pattern(regexp = "N[1-5]")
    String currentLevel;

}
