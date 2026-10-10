package kaito.jlpt.ktjlpt.controller;

import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamImportResponse;
import kaito.jlpt.ktjlpt.service.ExamImportService;
import kaito.jlpt.ktjlpt.service.excel.ExamTemplateGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminExamController {

    ExamImportService examImportService;
    ExamTemplateGenerator examTemplateGenerator;

    /**
     * POST /api/v1/admin/exams/import
     * Upload file Excel (.xlsx) để import đề thi JLPT.
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<ExamImportResponse> importExam(@RequestParam("file") MultipartFile file) {
        ExamImportResponse result = examImportService.importExam(file);
        return ApiResponse.<ExamImportResponse>builder()
                .result(result)
                .message(result.getMessage())
                .build();
    }

    /**
     * GET /api/v1/admin/exams/template
     * Tải file Excel template mẫu chuẩn để nhập đề thi.
     */
    @GetMapping("/template")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] templateBytes = examTemplateGenerator.generateTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=jlpt_exam_template.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(templateBytes.length)
                .body(templateBytes);
    }
}
