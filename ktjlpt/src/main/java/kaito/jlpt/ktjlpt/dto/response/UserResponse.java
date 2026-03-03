package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String email;
    String fullName;
    String currentLevel;
    String avatarUrl;

    Boolean active;
}
