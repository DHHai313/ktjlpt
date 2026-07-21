package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PassageRequest {

    /** Nội dung văn bản (TEXT, nullable — passage có thể chỉ là ảnh). */
    String content;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    String imageUrl;
}
