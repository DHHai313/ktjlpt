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
public class ExamSectionRequest {

    @NotBlank(message = "Exam ID is required")
    String examId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    String title;

    @NotBlank(message = "Section type is required")
    @Size(max = 20, message = "Section type must not exceed 20 characters")
    String sectionType;

    @NotNull(message = "Section order is required")
    Integer sectionOrder;

    Integer timeLimit;

    @Size(max = 500, message = "Audio URL must not exceed 500 characters")
    String audioUrl;
}
