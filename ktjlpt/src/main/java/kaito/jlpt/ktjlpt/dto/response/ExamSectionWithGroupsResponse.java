package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

/**
 * ExamSection kèm danh sách QuestionGroup (mỗi group có danh sách câu hỏi).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamSectionWithGroupsResponse {

    String id;
    String examId;
    String title;
    String sectionType;
    Integer sectionOrder;
    Integer timeLimit;
    String audioUrl;
    Instant createdAt;

    List<QuestionGroupWithQuestionsResponse> groups;
}
