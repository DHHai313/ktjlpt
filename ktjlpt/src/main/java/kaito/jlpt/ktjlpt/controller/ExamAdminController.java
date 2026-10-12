package kaito.jlpt.ktjlpt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.ExamUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamAdminDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamAdminSummaryResponse;
import kaito.jlpt.ktjlpt.dto.response.PageResponse;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.service.ExamAdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping({"/admin/exams", "/api/v1/admin/exams"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Exam Management", description = "APIs for managing exams by administrators")
public class ExamAdminController {

    ExamAdminService examAdminService;

    /**
     * 1. GET /admin/exams
     * Lấy danh sách tất cả đề thi (cả nháp lẫn đã publish) có phân trang và bộ lọc.
     */
    @Operation(summary = "Lấy danh sách tất cả đề thi",
            description = "Lấy danh sách đề thi có phân trang, mặc định sắp xếp createdAt giảm dần. Cho phép lọc theo level, published và tìm kiếm theo keyword.")
    @GetMapping
    public ApiResponse<PageResponse<ExamAdminSummaryResponse>> getExams(
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(required = false) Boolean published,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.<PageResponse<ExamAdminSummaryResponse>>builder()
                .result(examAdminService.getExams(level, published, keyword, pageable))
                .build();
    }

    /**
     * 2. GET /admin/exams/{id}
     * Lấy chi tiết đề thi đầy đủ (kèm đáp án đúng).
     */
    @Operation(summary = "Lấy chi tiết đề thi",
            description = "Lấy đầy đủ thông tin đề thi, sections, mondai, question groups, questions, options (kèm đáp án đúng). Sắp xếp theo orderNo ở mọi cấp.")
    @GetMapping("/{id}")
    public ApiResponse<ExamAdminDetailResponse> getExamDetail(@PathVariable String id) {
        return ApiResponse.<ExamAdminDetailResponse>builder()
                .result(examAdminService.getExamDetail(id))
                .build();
    }

    /**
     * 3a. PATCH /admin/exams/{id}/publish
     * Xuất bản đề thi (chuyển published = true).
     */
    @Operation(summary = "Xuất bản đề thi",
            description = "Kiểm tra toàn bộ điều kiện hợp lệ của đề thi và chuyển published thành true. Idempotent nếu đề đã publish.")
    @PatchMapping("/{id}/publish")
    public ApiResponse<ExamAdminSummaryResponse> publishExam(@PathVariable String id) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.publishExam(id))
                .message("Exam published successfully")
                .build();
    }

    /**
     * 3b. PATCH /admin/exams/{id}/unpublish
     * Hủy xuất bản đề thi (chuyển published = false).
     */
    @Operation(summary = "Hủy xuất bản đề thi",
            description = "Chuyển published thành false. Idempotent nếu đề chưa publish.")
    @PatchMapping("/{id}/unpublish")
    public ApiResponse<ExamAdminSummaryResponse> unpublishExam(@PathVariable String id) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.unpublishExam(id))
                .message("Exam unpublished successfully")
                .build();
    }

    /**
     * 4. PUT /admin/exams/{id}
     * Cập nhật thông tin cơ bản của đề thi (title, level, durationMinutes).
     */
    @Operation(summary = "Cập nhật thông tin cơ bản đề thi",
            description = "Cập nhật title (tối đa 255), level, durationMinutes (> 0). Không cho đổi nội dung câu hỏi.")
    @PutMapping("/{id}")
    public ApiResponse<ExamAdminSummaryResponse> updateExam(
            @PathVariable String id,
            @RequestBody @Valid ExamUpdateRequest request) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.updateExam(id, request))
                .message("Exam updated successfully")
                .build();
    }

    /**
     * 5. DELETE /admin/exams/{id}
     * Xóa đề thi (cascade xuống các bảng con). Từ chối nếu đã có lượt thi.
     */
    @Operation(summary = "Xóa đề thi",
            description = "Xóa đề thi và cascade xuống các phần con. Nếu đề thi đã có lượt làm bài thì từ chối xóa.")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteExam(@PathVariable String id) {
        examAdminService.deleteExam(id);
        return ApiResponse.<String>builder()
                .result("Exam deleted successfully")
                .message("Exam deleted successfully")
                .build();
    }
}
