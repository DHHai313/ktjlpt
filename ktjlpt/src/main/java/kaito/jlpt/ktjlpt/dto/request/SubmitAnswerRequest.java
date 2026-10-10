package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAnswerRequest {

    @NotNull(message = "questionId is required")
    String questionId;

    // NULL nếu user bỏ trống câu này
    String selectedOptionId;

    // User đánh dấu câu cần xem lại
    Boolean flagged;
}
