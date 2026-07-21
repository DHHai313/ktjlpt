package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamRequest {

    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Level is required")
    @Size(max = 10, message = "Level must not exceed 10 characters")
    String level;

    @NotNull(message = "Total time is required")
    Integer totalTime;

    @Builder.Default
    Integer totalScore = 180;

    @Builder.Default
    Integer passScore = 90;
}
