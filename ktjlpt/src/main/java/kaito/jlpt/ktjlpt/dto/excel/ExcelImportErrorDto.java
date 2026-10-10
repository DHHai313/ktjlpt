package kaito.jlpt.ktjlpt.dto.excel;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExcelImportErrorDto {
    int row;
    String column;
    String message;
}
