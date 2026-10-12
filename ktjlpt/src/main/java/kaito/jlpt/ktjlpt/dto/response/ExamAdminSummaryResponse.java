package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.JlptLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamAdminSummaryResponse {
    String id;
    String title;
    JlptLevel level;
    Integer durationMinutes;
    Boolean published;
    Instant createdAt;
    Instant updatedAt;
    Long totalSections;
    Long totalQuestions;
}
