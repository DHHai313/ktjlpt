package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.excel.ExcelImportErrorDto;
import kaito.jlpt.ktjlpt.dto.response.ExamImportResponse;
import kaito.jlpt.ktjlpt.entity.Exam;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.exception.ExcelValidationException;
import kaito.jlpt.ktjlpt.repository.ExamRepository;
import kaito.jlpt.ktjlpt.service.excel.ExamExcelParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExamImportService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    ExamRepository examRepository;
    ExamExcelParser examExcelParser;

    /**
     * Import đề thi từ file Excel (.xlsx).
     * Toàn bộ quá trình nằm trong 1 transaction — nếu có lỗi thì rollback hết.
     */
    @Transactional
    public ExamImportResponse importExam(MultipartFile file) {
        // 1. Validate file
        validateFile(file);

        // 2. Parse Excel -> Entity tree
        Exam exam;
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            exam = examExcelParser.parseExcel(workbook);

        } catch (ExcelValidationException e) {
            // Re-throw để GlobalExceptionHandler xử lý
            throw e;
        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            throw new AppException(ErrorCode.EXCEL_PARSE_ERROR);
        }

        // 3. Persist (CascadeType.ALL sẽ tự lưu toàn bộ cây con)
        exam = examRepository.save(exam);
        log.info("Imported exam '{}' with id={}", exam.getTitle(), exam.getId());

        // 4. Đếm tổng số câu hỏi
        int totalQuestions = exam.getSections().stream()
                .flatMap(s -> s.getMondaiList().stream())
                .flatMap(m -> m.getQuestionGroups().stream())
                .flatMap(g -> g.getQuestions().stream())
                .mapToInt(q -> 1)
                .sum();

        return ExamImportResponse.builder()
                .examId(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel().name())
                .totalSections(exam.getSections().size())
                .totalQuestions(totalQuestions)
                .message("Import thành công")
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.EXCEL_INVALID_FORMAT);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new AppException(ErrorCode.EXCEL_INVALID_FORMAT);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.EXCEL_FILE_TOO_LARGE);
        }
    }
}
