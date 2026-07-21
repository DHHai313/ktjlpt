package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAnswerRequest {

    @NotBlank(message = "Question ID is required")
    String questionId;

    /**
     * ID của option được chọn. Nullable — null nghĩa là bỏ qua câu hỏi này.
     */
    String selectedOptionId;
}
