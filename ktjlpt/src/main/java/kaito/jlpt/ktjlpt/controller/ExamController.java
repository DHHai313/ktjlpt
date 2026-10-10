package kaito.jlpt.ktjlpt.controller;

import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamSummaryResponse;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.service.ExamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

    ExamService examService;

    /**
     * GET /api/v1/exams
     * Lấy danh sách tất cả đề thi đã published (tuỳ chọn lọc theo level)
     */
    @GetMapping
    public ApiResponse<List<ExamSummaryResponse>> listExams(
            @RequestParam(required = false) JlptLevel level) {

        List<ExamSummaryResponse> exams = (level != null)
                ? examService.getPublishedExamsByLevel(level)
                : examService.getAllPublishedExams();

        return ApiResponse.<List<ExamSummaryResponse>>builder()
                .result(exams)
                .build();
    }

    /**
     * GET /api/v1/exams/{examId}
     * Lấy chi tiết đề thi để làm bài (không kèm đáp án)
     */
    @GetMapping("/{examId}")
    public ApiResponse<ExamDetailResponse> getExamDetail(@PathVariable String examId) {
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.getExamForAttempt(examId))
                .build();
    }
}
