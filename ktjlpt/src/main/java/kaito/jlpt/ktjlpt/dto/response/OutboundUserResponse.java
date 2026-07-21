package kaito.jlpt.ktjlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboundUserResponse {
    String id;
    String email;
    String name;

    @JsonProperty("given_name")
    String givenName;

    /**
     * Google API trả về field tên là "picture", không phải "avatar_url".
     */
    @JsonProperty("picture")
    String picture;

    @JsonProperty("verified_email")
    Boolean verifiedEmail;
}
