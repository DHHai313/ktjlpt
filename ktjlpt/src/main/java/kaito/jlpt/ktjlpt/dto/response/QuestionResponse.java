package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {

    String id;
    String groupId;
    String passageId;
    String content;
    String imageUrl;
    Integer questionOrder;
    Instant createdAt;

    /** Danh sách đáp án. isCorrect bị null nếu caller không phải ADMIN. */
    List<QuestionOptionResponse> options;
}
