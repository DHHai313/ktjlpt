package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResponse {

    String id;
    String title;
    String level;
    Integer totalTime;
    Integer totalScore;
    Integer passScore;
    Instant createdAt;

    /** Số lượng section trong exam (được tính thủ công khi map). */
    int sectionCount;
}
