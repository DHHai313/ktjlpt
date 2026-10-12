package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.ExamUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.*;
import kaito.jlpt.ktjlpt.entity.Exam;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.ExamRepository;
import kaito.jlpt.ktjlpt.service.ExamAdminService;
import kaito.jlpt.ktjlpt.service.ExamService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

    ExamService examService;
    ExamAdminService examAdminService;
    ExamRepository examRepository;

    /**
     * GET /api/v1/exams
     * Lấy danh sách đề thi:
     * - Với User / Khách: luôn chỉ trả về các đề đã published (có thể lọc theo level).
     * - Với Admin: xem được tất cả đề thi (bao gồm đề vừa import chưa publish), có thể lọc theo published (true/false) hoặc level.
     */
    @GetMapping
    public ApiResponse<List<ExamSummaryResponse>> listExams(
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(required = false) Boolean published) {

        boolean isAdmin = isCurrentUserAdmin();
        List<ExamSummaryResponse> exams;

        if (isAdmin) {
            exams = examService.getExams(level, published);
        } else {
            exams = (level != null)
                    ? examService.getPublishedExamsByLevel(level)
                    : examService.getAllPublishedExams();
        }

        return ApiResponse.<List<ExamSummaryResponse>>builder()
                .result(exams)
                .build();
    }

    /**
     * GET /api/v1/exams/admin/all
     * API chuyên dụng cho Admin lấy toàn bộ danh sách đề thi kèm trạng thái published.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ExamSummaryResponse>> getAllExamsForAdmin(
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(required = false) Boolean published) {
        return ApiResponse.<List<ExamSummaryResponse>>builder()
                .result(examService.getExams(level, published))
                .build();
    }

    /**
     * GET /api/v1/exams/{examId}
     * Lấy chi tiết đề thi để làm bài hoặc xem trước (preview).
     * Nếu là Admin: cho phép xem kể cả khi đề chưa publish (vừa import xong).
     */
    @GetMapping("/{examId}")
    public ApiResponse<ExamDetailResponse> getExamDetail(@PathVariable String examId) {
        boolean isAdmin = isCurrentUserAdmin();
        return ApiResponse.<ExamDetailResponse>builder()
                .result(examService.getExamForAttempt(examId, isAdmin))
                .build();
    }

    /**
     * GET /api/v1/exams/{examId}/admin
     * Xem đầy đủ chi tiết đề thi dành cho Admin (bao gồm cả đáp án đúng và giải thích).
     */
    @GetMapping("/{examId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminDetailResponse> getExamAdminDetail(@PathVariable String examId) {
        return ApiResponse.<ExamAdminDetailResponse>builder()
                .result(examAdminService.getExamDetail(examId))
                .build();
    }

    /**
     * PATCH /api/v1/exams/{examId}/publish
     * Xuất bản đề thi (chuyển published = true sau khi kiểm tra hợp lệ cấu trúc đề thi).
     */
    @PatchMapping("/{examId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminSummaryResponse> publishExam(@PathVariable String examId) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.publishExam(examId))
                .message("Đề thi đã được xuất bản thành công (Published)")
                .build();
    }

    /**
     * PATCH /api/v1/exams/{examId}/unpublish hoặc /unpublic
     * Hủy xuất bản đề thi (chuyển published = false về trạng thái nháp).
     */
    @PatchMapping(value = {"/{examId}/unpublish", "/{examId}/unpublic"})
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminSummaryResponse> unpublishExam(@PathVariable String examId) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.unpublishExam(examId))
                .message("Đề thi đã chuyển về trạng thái nháp (Unpublished)")
                .build();
    }

    /**
     * PATCH /api/v1/exams/{examId}/toggle-publish
     * Bật/tắt trạng thái xuất bản của đề thi (nếu đang published thì unpublish và ngược lại).
     */
    @PatchMapping("/{examId}/toggle-publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminSummaryResponse> togglePublishExam(@PathVariable String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        ExamAdminSummaryResponse result = Boolean.TRUE.equals(exam.getPublished())
                ? examAdminService.unpublishExam(examId)
                : examAdminService.publishExam(examId);

        String message = Boolean.TRUE.equals(result.getPublished())
                ? "Đề thi đã được xuất bản thành công (Published)"
                : "Đề thi đã chuyển về trạng thái nháp (Unpublished)";

        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(result)
                .message(message)
                .build();
    }

    /**
     * PATCH /api/v1/exams/{examId}/status
     * Cập nhật trạng thái xuất bản theo tham số boolean (?published=true / false).
     */
    @PatchMapping("/{examId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminSummaryResponse> updatePublishStatus(
            @PathVariable String examId,
            @RequestParam boolean published) {
        ExamAdminSummaryResponse result = published
                ? examAdminService.publishExam(examId)
                : examAdminService.unpublishExam(examId);

        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(result)
                .message(published ? "Đề thi đã được xuất bản (Published)" : "Đề thi đã hủy xuất bản (Unpublished)")
                .build();
    }

    /**
     * PUT /api/v1/exams/{examId}
     * Cập nhật thông tin cơ bản của đề thi (title, level, durationMinutes).
     */
    @PutMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExamAdminSummaryResponse> updateExam(
            @PathVariable String examId,
            @RequestBody @Valid ExamUpdateRequest request) {
        return ApiResponse.<ExamAdminSummaryResponse>builder()
                .result(examAdminService.updateExam(examId, request))
                .message("Cập nhật thông tin đề thi thành công")
                .build();
    }

    /**
     * DELETE /api/v1/exams/{examId}
     * Xóa đề thi (từ chối nếu đã có lượt làm bài).
     */
    @DeleteMapping("/{examId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteExam(@PathVariable String examId) {
        examAdminService.deleteExam(examId);
        return ApiResponse.<String>builder()
                .result("Đề thi đã được xóa thành công")
                .message("Đề thi đã được xóa thành công")
                .build();
    }

    // ─── Helper method kiểm tra quyền ADMIN ─────────────────────────────
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
    }
}
