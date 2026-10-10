package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.constraints.NotNull;
import kaito.jlpt.ktjlpt.enums.AttemptMode;
import kaito.jlpt.ktjlpt.enums.AttemptScope;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StartAttemptRequest {

    @NotNull(message = "mode is required")
    AttemptMode mode;

    @NotNull(message = "scope is required")
    AttemptScope scope;

    // Bắt buộc khi scope = SECTION
    String sectionId;
}
