package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttemptResponse {

    String id;
    String userId;
    String examId;
    Integer totalScore;
    Instant startedAt;
    Instant completedAt;
    String status;
}
