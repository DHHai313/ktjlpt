package kaito.jlpt.ktjlpt.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkSubmitAnswerRequest {

    @NotNull(message = "Answers list is required")
    @Valid
    List<SubmitAnswerRequest> answers;
}
