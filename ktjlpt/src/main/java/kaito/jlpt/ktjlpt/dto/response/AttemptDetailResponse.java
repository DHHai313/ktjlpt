package kaito.jlpt.ktjlpt.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Chi tiết của một lần thi, bao gồm toàn bộ danh sách câu trả lời.
 * Kế thừa AttemptResponse thay vì dùng composition để giữ cùng cấu trúc field.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttemptDetailResponse extends AttemptResponse {

    List<UserAnswerResponse> answers;
}
