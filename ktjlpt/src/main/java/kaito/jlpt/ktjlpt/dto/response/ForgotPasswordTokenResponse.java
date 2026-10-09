package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForgotPasswordTokenResponse {

    // Reset token tạm thời (UUID), có hiệu lực 10 phút.
    // FE giữ token này và gửi kèm ở bước reset-password.
    String resetToken;
}
