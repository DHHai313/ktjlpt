package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.ExamRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamWithSectionsResponse;
import kaito.jlpt.ktjlpt.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /**
     * GET /exams — public, lấy danh sách tất cả đề thi.
     */
    @GetMapping
    ApiResponse<List<ExamResponse>> getAllExams() {
        return ApiResponse.<List<ExamResponse>>builder()
                .result(examService.getAllExams())
                .build();
    }

    /**
     * GET /exams/{id} — public, xem thông tin cơ bản của đề thi.
     */
    @GetMapping("/{id}")
    ApiResponse<ExamResponse> getExamById(@PathVariable String id) {
        return ApiResponse.<ExamResponse>builder()
                .result(examService.getExamById(id))
                .build();
    }

    /**
     * GET /exams/{id}/full — authenticated, xem toàn bộ cấu trúc đề thi.
     * isCorrect bị ẩn nếu caller không phải ADMIN.
     */
    @GetMapping("/{id}/full")
    ApiResponse<ExamWithSectionsResponse> getExamWithFullContent(@PathVariable String id) {
        return ApiResponse.<ExamWithSectionsResponse>builder()
                .result(examService.getExamWithFullContent(id))
                .build();
    }

    /**
     * GET /exams/level/{level} — public, lọc đề thi theo level (N1, N2, …).
     */
    @GetMapping("/level/{level}")
    ApiResponse<List<ExamResponse>> getExamsByLevel(@PathVariable String level) {
        return ApiResponse.<List<ExamResponse>>builder()
                .result(examService.getExamsByLevel(level))
                .build();
    }

    /**
     * POST /exams — ADMIN only, tạo đề thi mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ExamResponse> createExam(@Valid @RequestBody ExamRequest request) {
        return ApiResponse.<ExamResponse>builder()
                .result(examService.createExam(request))
                .build();
    }

    /**
     * PUT /exams/{id} — ADMIN only, cập nhật thông tin đề thi.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ExamResponse> updateExam(
            @PathVariable String id,
            @Valid @RequestBody ExamRequest request) {
        return ApiResponse.<ExamResponse>builder()
                .result(examService.updateExam(id, request))
                .build();
    }

    /**
     * DELETE /exams/{id} — ADMIN only, xóa đề thi.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteExam(@PathVariable String id) {
        examService.deleteExam(id);
        return ApiResponse.<String>builder()
                .result("Exam deleted successfully")
                .build();
    }
}
