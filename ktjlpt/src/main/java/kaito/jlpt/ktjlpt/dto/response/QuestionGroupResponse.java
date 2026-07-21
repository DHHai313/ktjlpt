package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionGroupResponse {

    String id;
    String sectionId;
    String passageId;
    String title;
    Integer groupOrder;
    String description;
    Instant createdAt;
}
