package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.QuestionGroupRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.QuestionGroupResponse;
import kaito.jlpt.ktjlpt.service.QuestionGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/question-groups")
@RequiredArgsConstructor
public class QuestionGroupController {

    private final QuestionGroupService questionGroupService;

    /**
     * GET /question-groups/section/{sectionId} — public, lấy danh sách groups của một section.
     */
    @GetMapping("/section/{sectionId}")
    ApiResponse<List<QuestionGroupResponse>> getGroupsBySectionId(@PathVariable String sectionId) {
        return ApiResponse.<List<QuestionGroupResponse>>builder()
                .result(questionGroupService.getGroupsBySectionId(sectionId))
                .build();
    }

    /**
     * GET /question-groups/{id} — public, xem chi tiết một group.
     */
    @GetMapping("/{id}")
    ApiResponse<QuestionGroupResponse> getGroupById(@PathVariable String id) {
        return ApiResponse.<QuestionGroupResponse>builder()
                .result(questionGroupService.getGroupById(id))
                .build();
    }

    /**
     * POST /question-groups — ADMIN only, tạo group mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionGroupResponse> createGroup(@Valid @RequestBody QuestionGroupRequest request) {
        return ApiResponse.<QuestionGroupResponse>builder()
                .result(questionGroupService.createGroup(request))
                .build();
    }

    /**
     * PUT /question-groups/{id} — ADMIN only, cập nhật group.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<QuestionGroupResponse> updateGroup(
            @PathVariable String id,
            @Valid @RequestBody QuestionGroupRequest request) {
        return ApiResponse.<QuestionGroupResponse>builder()
                .result(questionGroupService.updateGroup(id, request))
                .build();
    }

    /**
     * DELETE /question-groups/{id} — ADMIN only, xóa group.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deleteGroup(@PathVariable String id) {
        questionGroupService.deleteGroup(id);
        return ApiResponse.<String>builder()
                .result("Question group deleted successfully")
                .build();
    }
}
