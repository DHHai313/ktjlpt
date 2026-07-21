package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAnswerResponse {

    String id;
    String attemptId;
    String questionId;
    String selectedOptionId;
    Boolean isCorrect;
}
