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
public class QuestionGroupRequest {

    @NotBlank(message = "Section ID is required")
    String sectionId;

    /** Passage dùng chung cho cả group (nullable). */
    String passageId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @NotNull(message = "Group order is required")
    Integer groupOrder;

    /** Mô tả/hướng dẫn cho nhóm câu hỏi (TEXT, nullable). */
    String description;
}
