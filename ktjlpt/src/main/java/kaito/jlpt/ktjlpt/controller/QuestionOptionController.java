package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.QuestionOptionRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.QuestionOptionResponse;
import kaito.jlpt.ktjlpt.service.QuestionOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/question-options")
@RequiredArgsConstructor
public class QuestionOptionController {

    private final QuestionOptionService questionOptionService;

    /**
     * GET /question-options/question/{questionId} — authenticated.
     * ADMIN thấy isCorrect; USER thấy null cho isCorrect.
     */
    @GetMapping("/question/{questionId}")
    ApiResponse<List<QuestionOptionResponse>> getOptionsByQuestionId(@PathVariable String questionId) {
        return ApiResponse.<List<QuestionOptionResponse>>builder()
                .result(questionOptionService.getOptionsByQuestionId(questionId))
                .build();
    }

    /**
     * POST /question-options — ADMIN only, tạo option mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionOptionResponse> createOption(@Valid @RequestBody QuestionOptionRequest request) {
        return ApiResponse.<QuestionOptionResponse>builder()
                .result(questionOptionService.createOption(request))
                .build();
    }

    /**
     * PUT /question-options/{id} — ADMIN only, cập nhật option.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionOptionResponse> updateOption(
            @PathVariable String id,
            @Valid @RequestBody QuestionOptionRequest request) {
        return ApiResponse.<QuestionOptionResponse>builder()
                .result(questionOptionService.updateOption(id, request))
                .build();
    }

    /**
     * DELETE /question-options/{id} — ADMIN only, xóa option.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteOption(@PathVariable String id) {
        questionOptionService.deleteOption(id);
        return ApiResponse.<String>builder()
                .result("Option deleted successfully")
                .build();
    }
}
