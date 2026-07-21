package kaito.jlpt.ktjlpt.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogoutRequest {
    /**
     * Access Token được gửi lên trong body để đưa vào blacklist Redis.
     * Refresh Token được đọc từ HttpOnly Cookie — không cần gửi trong body.
     */
    @JsonProperty("access_token")
    String accessToken;
}
