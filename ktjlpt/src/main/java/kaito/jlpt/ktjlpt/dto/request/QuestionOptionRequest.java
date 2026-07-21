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
public class QuestionOptionRequest {

    @NotBlank(message = "Question ID is required")
    String questionId;

    @NotBlank(message = "Option content is required")
    String content;

    @NotNull(message = "Option order is required")
    Integer optionOrder;

    /** Đánh dấu đây có phải đáp án đúng không, mặc định false. */
    @Builder.Default
    Boolean isCorrect = false;
}
