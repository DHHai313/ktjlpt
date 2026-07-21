package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Exam kèm toàn bộ cấu trúc: sections → groups → questions → options.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamWithSectionsResponse {

    String id;
    String title;
    String level;
    Integer totalTime;
    Integer totalScore;
    Integer passScore;
    int sectionCount;

    List<ExamSectionWithGroupsResponse> sections;
}
