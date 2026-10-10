package kaito.jlpt.ktjlpt.exception;

import kaito.jlpt.ktjlpt.dto.excel.ExcelImportErrorDto;
import lombok.Getter;

import java.util.List;

@Getter
public class ExcelValidationException extends RuntimeException {
    private final List<ExcelImportErrorDto> errors;

    public ExcelValidationException(List<ExcelImportErrorDto> errors) {
        super("Excel validation failed with " + errors.size() + " error(s)");
        this.errors = errors;
    }
}
