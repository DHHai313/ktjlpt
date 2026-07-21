package kaito.jlpt.ktjlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionOptionResponse {

    String id;
    String questionId;
    String content;
    Integer optionOrder;

    /**
     * Chỉ trả về cho ADMIN. Với USER, field này sẽ là null và bị loại bỏ
     * bởi @JsonInclude(NON_NULL) ở cấp response tổng.
     * Service sẽ set null khi role != ADMIN.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean isCorrect;

    Instant createdAt;
}
