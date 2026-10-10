package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.StartAttemptRequest;
import kaito.jlpt.ktjlpt.dto.request.SubmitAnswerRequest;
import kaito.jlpt.ktjlpt.dto.request.SubmitAttemptRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.AttemptResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamReviewResponse;
import kaito.jlpt.ktjlpt.entity.User;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.UserRepository;
import kaito.jlpt.ktjlpt.service.AttemptService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttemptController {

    AttemptService attemptService;
    UserRepository userRepository;

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Lấy userId của user đang đăng nhập.
     * JWT subject = username, sau đó tra DB lấy id.
     */
    private String getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.getId();
    }

    // ─── Attempt endpoints ────────────────────────────────────────────────────

    /**
     * POST /api/v1/exams/{examId}/attempts
     * Bắt đầu lượt làm bài mới
     */
    @PostMapping("/{examId}/attempts")
    public ApiResponse<AttemptResponse> startAttempt(
            @PathVariable String examId,
            @RequestBody @Valid StartAttemptRequest request) {

        String userId = getCurrentUserId();
        return ApiResponse.<AttemptResponse>builder()
                .result(attemptService.startAttempt(userId, examId, request))
                .build();
    }

    /**
     * GET /api/v1/exams/{examId}/attempts
     * Lấy lịch sử tất cả lượt làm bài của user với đề thi này
     */
    @GetMapping("/{examId}/attempts")
    public ApiResponse<List<AttemptResponse>> getUserAttempts(@PathVariable String examId) {
        String userId = getCurrentUserId();
        return ApiResponse.<List<AttemptResponse>>builder()
                .result(attemptService.getUserAttempts(userId))
                .build();
    }

    /**
     * PUT /api/v1/exams/{examId}/attempts/{attemptId}/answer
     * Lưu / cập nhật câu trả lời trong khi làm bài (auto-save)
     */
    @PutMapping("/{examId}/attempts/{attemptId}/answer")
    public ApiResponse<String> saveAnswer(
            @PathVariable String examId,
            @PathVariable String attemptId,
            @RequestBody @Valid SubmitAnswerRequest request) {

        String userId = getCurrentUserId();
        attemptService.saveAnswer(userId, attemptId, request);
        return ApiResponse.<String>builder()
                .result("Answer saved")
                .build();
    }

    /**
     * POST /api/v1/exams/{examId}/attempts/{attemptId}/submit
     * Nộp toàn bộ bài thi
     */
    @PostMapping("/{examId}/attempts/{attemptId}/submit")
    public ApiResponse<AttemptResponse> submitAttempt(
            @PathVariable String examId,
            @PathVariable String attemptId,
            @RequestBody @Valid SubmitAttemptRequest request) {

        String userId = getCurrentUserId();
        return ApiResponse.<AttemptResponse>builder()
                .result(attemptService.submitAttempt(userId, attemptId, request))
                .build();
    }

    /**
     * GET /api/v1/exams/{examId}/attempts/{attemptId}/review
     * Xem lại kết quả và đáp án sau khi nộp bài
     */
    @GetMapping("/{examId}/attempts/{attemptId}/review")
    public ApiResponse<ExamReviewResponse> getReview(
            @PathVariable String examId,
            @PathVariable String attemptId) {

        String userId = getCurrentUserId();
        return ApiResponse.<ExamReviewResponse>builder()
                .result(attemptService.getReview(userId, attemptId))
                .build();
    }
}
