package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionRequest {

    @NotBlank(message = "Group ID is required")
    String groupId;

    /** Passage riêng cho câu hỏi (nullable — bổ sung ngoài passage nhóm). */
    String passageId;

    @NotBlank(message = "Question content is required")
    String content;

    String imageUrl;

    @NotNull(message = "Question order is required")
    Integer questionOrder;
}
