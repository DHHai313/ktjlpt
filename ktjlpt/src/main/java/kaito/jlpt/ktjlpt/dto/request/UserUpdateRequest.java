package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @NotBlank
    @Size(min = 2, max=100)
    String fullName;
    @Size(max=5)
    @Pattern(regexp = "N[1-5]")
    String currentLevel;
    @URL
    @Size(max = 512)
    String avatarUrl;


}
