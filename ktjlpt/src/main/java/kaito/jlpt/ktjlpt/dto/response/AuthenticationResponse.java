package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    /**
     * Access Token ngắn hạn (15 phút) — trả về trong JSON body để frontend lưu vào Local Storage.
     * Refresh Token KHÔNG được trả về ở đây; nó được set vào HttpOnly Cookie.
     */
    String accessToken;
}
