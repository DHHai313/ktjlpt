package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamUpdateRequest {

    @NotBlank(message = "TITLE_INVALID")
    @Size(max = 255, message = "TITLE_INVALID")
    String title;

    @NotNull(message = "LEVEL_INVALID")
    JlptLevel level;

    @NotNull(message = "DURATION_INVALID")
    @Min(value = 1, message = "DURATION_INVALID")
    Integer durationMinutes;
}
