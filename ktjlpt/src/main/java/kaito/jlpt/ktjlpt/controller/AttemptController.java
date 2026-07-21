package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.BulkSubmitAnswerRequest;
import kaito.jlpt.ktjlpt.dto.request.StartAttemptRequest;
import kaito.jlpt.ktjlpt.dto.request.SubmitAnswerRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.AttemptDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.AttemptResponse;
import kaito.jlpt.ktjlpt.dto.response.UserAnswerResponse;
import kaito.jlpt.ktjlpt.service.AttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    /**
     * POST /attempts/start — authenticated USER, bắt đầu lần thi mới.
     */
    @PostMapping("/start")
    ApiResponse<AttemptResponse> startAttempt(@Valid @RequestBody StartAttemptRequest request) {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<AttemptResponse>builder()
                .result(attemptService.startAttempt(request.getExamId(), userEmail))
                .build();
    }

    /**
     * POST /attempts/{attemptId}/answers — authenticated USER, nộp một câu trả lời.
     */
    @PostMapping("/{attemptId}/answers")
    ApiResponse<UserAnswerResponse> submitAnswer(
            @PathVariable String attemptId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<UserAnswerResponse>builder()
                .result(attemptService.submitAnswer(attemptId, request, userEmail))
                .build();
    }

    /**
     * POST /attempts/{attemptId}/answers/bulk — authenticated USER, nộp nhiều câu trả lời.
     */
    @PostMapping("/{attemptId}/answers/bulk")
    ApiResponse<List<UserAnswerResponse>> bulkSubmitAnswers(
            @PathVariable String attemptId,
            @Valid @RequestBody BulkSubmitAnswerRequest request) {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<List<UserAnswerResponse>>builder()
                .result(attemptService.bulkSubmitAnswers(attemptId, request.getAnswers(), userEmail))
                .build();
    }

    /**
     * POST /attempts/{attemptId}/complete — authenticated USER, hoàn thành lần thi.
     * Server tự tính điểm dựa trên số câu đúng.
     */
    @PostMapping("/{attemptId}/complete")
    ApiResponse<AttemptResponse> completeAttempt(@PathVariable String attemptId) {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<AttemptResponse>builder()
                .result(attemptService.completeAttempt(attemptId, userEmail))
                .build();
    }

    /**
     * GET /attempts/my — authenticated USER, lịch sử thi của chính mình.
     */
    @GetMapping("/my")
    ApiResponse<List<AttemptResponse>> getMyAttempts() {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<List<AttemptResponse>>builder()
                .result(attemptService.getMyAttempts(userEmail))
                .build();
    }

    /**
     * GET /attempts/{attemptId} — authenticated USER, xem chi tiết một lần thi.
     * Chỉ xem của chính mình (service kiểm tra ownership).
     */
    @GetMapping("/{attemptId}")
    ApiResponse<AttemptDetailResponse> getAttemptDetail(@PathVariable String attemptId) {
        String userEmail = getCurrentUserEmail();
        return ApiResponse.<AttemptDetailResponse>builder()
                .result(attemptService.getAttemptDetail(attemptId, userEmail))
                .build();
    }

    /**
     * GET /attempts — ADMIN only, xem tất cả lần thi của mọi user.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<AttemptResponse>> getAllAttempts() {
        return ApiResponse.<List<AttemptResponse>>builder()
                .result(attemptService.getAllAttempts())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lấy email của user đang đăng nhập từ SecurityContext.
     * JWT subject được set là email trong AuthenticationService.
     */
    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
