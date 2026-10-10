package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubmitAttemptRequest {

    // Danh sách tất cả câu trả lời khi nộp bài
    @NotEmpty(message = "answers must not be empty")
    List<SubmitAnswerRequest> answers;

    // Tổng thời gian làm bài thực tế (giây)
    Integer timeSpentSeconds;
}
