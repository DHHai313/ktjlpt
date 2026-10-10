package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.AttemptMode;
import kaito.jlpt.ktjlpt.enums.AttemptScope;
import kaito.jlpt.ktjlpt.enums.AttemptStatus;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.enums.SectionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttemptResponse {
    String id;
    String examId;
    String examTitle;
    JlptLevel examLevel;
    AttemptMode mode;
    AttemptScope scope;
    String sectionId;
    SectionType sectionType;
    AttemptStatus status;
    Integer durationMinutes;
    Instant startedAt;
    Instant expiresAt;
    Instant submittedAt;
    Integer timeSpentSeconds;
    Integer correctCount;
    Integer totalQuestions;
}
