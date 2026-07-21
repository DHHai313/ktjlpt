package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamSectionResponse {

    String id;
    String examId;
    String title;
    String sectionType;
    Integer sectionOrder;
    Integer timeLimit;
    String audioUrl;
    Instant createdAt;
}
