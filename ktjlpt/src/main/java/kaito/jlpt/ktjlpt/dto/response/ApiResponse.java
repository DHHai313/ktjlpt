package kaito.jlpt.ktjlpt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)//khi chuyen sang json ko in field null
public class ApiResponse <T> {
    private int code=1000;
    private String message;
    private T result;

}
