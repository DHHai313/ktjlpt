package kaito.jlpt.ktjlpt.controller;

import jakarta.validation.Valid;
import kaito.jlpt.ktjlpt.dto.request.PassageRequest;
import kaito.jlpt.ktjlpt.dto.response.ApiResponse;
import kaito.jlpt.ktjlpt.dto.response.PassageResponse;
import kaito.jlpt.ktjlpt.service.PassageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/passages")
@RequiredArgsConstructor
public class PassageController {

    private final PassageService passageService;

    /**
     * GET /passages — ADMIN only, xem tất cả passages.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<PassageResponse>> getAllPassages() {
        return ApiResponse.<List<PassageResponse>>builder()
                .result(passageService.getAllPassages())
                .build();
    }

    /**
     * GET /passages/{id} — authenticated, xem chi tiết một passage.
     */
    @GetMapping("/{id}")
    ApiResponse<PassageResponse> getPassageById(@PathVariable String id) {
        return ApiResponse.<PassageResponse>builder()
                .result(passageService.getPassageById(id))
                .build();
    }

    /**
     * POST /passages — ADMIN only, tạo passage mới.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PassageResponse> createPassage(@Valid @RequestBody PassageRequest request) {
        return ApiResponse.<PassageResponse>builder()
                .result(passageService.createPassage(request))
                .build();
    }

    /**
     * PUT /passages/{id} — ADMIN only, cập nhật passage.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<PassageResponse> updatePassage(
            @PathVariable String id,
            @Valid @RequestBody PassageRequest request) {
        return ApiResponse.<PassageResponse>builder()
                .result(passageService.updatePassage(id, request))
                .build();
    }

    /**
     * DELETE /passages/{id} — ADMIN only, xóa passage.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<String> deletePassage(@PathVariable String id) {
        passageService.deletePassage(id);
        return ApiResponse.<String>builder()
                .result("Passage deleted successfully")
                .build();
    }
}
