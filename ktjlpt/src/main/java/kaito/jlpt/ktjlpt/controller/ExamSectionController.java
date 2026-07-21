package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.ExamSectionRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamSectionResponse;
import kaito.jlpt.ktjlpt.service.ExamSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/exam-sections")
@RequiredArgsConstructor
public class ExamSectionController {

    private final ExamSectionService examSectionService;

    /**
     * GET /exam-sections/exam/{examId} — public, lấy danh sách sections của một exam.
     */
    @GetMapping("/exam/{examId}")
    ApiResponse<List<ExamSectionResponse>> getSectionsByExamId(@PathVariable String examId) {
        return ApiResponse.<List<ExamSectionResponse>>builder()
                .result(examSectionService.getSectionsByExamId(examId))
                .build();
    }

    /**
     * GET /exam-sections/{id} — public, xem chi tiết một section.
     */
    @GetMapping("/{id}")
    ApiResponse<ExamSectionResponse> getSectionById(@PathVariable String id) {
        return ApiResponse.<ExamSectionResponse>builder()
                .result(examSectionService.getSectionById(id))
                .build();
    }

    /**
     * POST /exam-sections — ADMIN only, tạo section mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ExamSectionResponse> createSection(@Valid @RequestBody ExamSectionRequest request) {
        return ApiResponse.<ExamSectionResponse>builder()
                .result(examSectionService.createSection(request))
                .build();
    }

    /**
     * PUT /exam-sections/{id} — ADMIN only, cập nhật section.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ExamSectionResponse> updateSection(
            @PathVariable String id,
            @Valid @RequestBody ExamSectionRequest request) {
        return ApiResponse.<ExamSectionResponse>builder()
                .result(examSectionService.updateSection(id, request))
                .build();
    }

    /**
     * DELETE /exam-sections/{id} — ADMIN only, xóa section.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteSection(@PathVariable String id) {
        examSectionService.deleteSection(id);
        return ApiResponse.<String>builder()
                .result("Section deleted successfully")
                .build();
    }
}
