package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamImportResponse {
    String examId;
    String title;
    String level;
    int totalSections;
    int totalQuestions;
    String message;
}
