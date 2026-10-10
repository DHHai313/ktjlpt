package kaito.jlpt.ktjlpt.dto.excel;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO map dữ liệu từ Sheet "Exam_Info" trong file Excel.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamMetaSheetDto {
    /** Tiêu đề đề thi */
    String title;
    /** Cấp độ JLPT (N1..N5) */
    String level;
    /** Thời gian làm bài (phút) */
    Integer durationMinutes;
}
