package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.JlptLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamSummaryResponse {
    String id;
    String title;
    JlptLevel level;
    Integer durationMinutes;
    Boolean published;
    Instant createdAt;
}
