package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

/**
 * QuestionGroup kèm danh sách Question (mỗi câu hỏi có danh sách options).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionGroupWithQuestionsResponse {

    String id;
    String sectionId;
    String passageId;
    String title;
    Integer groupOrder;
    String description;
    Instant createdAt;

    List<QuestionResponse> questions;
}
