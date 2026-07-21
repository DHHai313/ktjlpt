package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.QuestionRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.QuestionResponse;
import kaito.jlpt.ktjlpt.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * GET /questions/group/{groupId} — public, lấy danh sách câu hỏi của một group.
     * isCorrect bị ẩn nếu caller không phải ADMIN.
     */
    @GetMapping("/group/{groupId}")
    ApiResponse<List<QuestionResponse>> getQuestionsByGroupId(@PathVariable String groupId) {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(questionService.getQuestionsByGroupId(groupId))
                .build();
    }

    /**
     * GET /questions/{id} — authenticated, xem chi tiết câu hỏi kèm options.
     * isCorrect bị ẩn nếu caller không phải ADMIN.
     */
    @GetMapping("/{id}")
    ApiResponse<QuestionResponse> getQuestionById(@PathVariable String id) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.getQuestionById(id))
                .build();
    }

    /**
     * POST /questions — ADMIN only, tạo câu hỏi mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.createQuestion(request))
                .build();
    }

    /**
     * PUT /questions/{id} — ADMIN only, cập nhật câu hỏi.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionResponse> updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionRequest request) {
        return ApiResponse.<QuestionResponse>builder()
                .result(questionService.updateQuestion(id, request))
                .build();
    }

    /**
     * DELETE /questions/{id} — ADMIN only, xóa câu hỏi.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ApiResponse.<String>builder()
                .result("Question deleted successfully")
                .build();
    }
}
