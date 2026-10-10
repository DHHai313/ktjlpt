package kaito.jlpt.ktjlpt.service.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class ExcelHelper {

    private ExcelHelper() {
    }

    /**
     * Đọc cell dưới dạng String, trả về null nếu cell trống.
     */
    public static String readString(Row row, int colIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                yield val.isEmpty() ? null : val;
            }
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : val;
                } catch (Exception e) {
                    yield String.valueOf((int) cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    /**
     * Đọc cell dưới dạng Integer, trả về null nếu cell trống hoặc không phải số.
     */
    public static Integer readInteger(Row row, int colIndex) {
        if (row == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) yield null;
                try {
                    yield Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }
}
