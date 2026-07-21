package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.PassageRequest;
import kaito.jlpt.ktjlpt.dto.response.PassageResponse;
import kaito.jlpt.ktjlpt.entity.Passage;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.PassageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PassageService {

    PassageRepository passageRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    public PassageResponse createPassage(PassageRequest request) {
        log.info("createPassage");
        Passage passage = Passage.builder()
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();
        return mapToResponse(passageRepository.save(passage));
    }

    public PassageResponse updatePassage(String passageId, PassageRequest request) {
        log.info("updatePassage - passageId: {}", passageId);
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
        passage.setContent(request.getContent());
        passage.setImageUrl(request.getImageUrl());
        return mapToResponse(passageRepository.save(passage));
    }

    public void deletePassage(String passageId) {
        log.info("deletePassage - passageId: {}", passageId);
        if (!passageRepository.existsById(passageId)) {
            throw new AppException(ErrorCode.PASSAGE_NOT_FOUND);
        }
        passageRepository.deleteById(passageId);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    public List<PassageResponse> getAllPassages() {
        return passageRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PassageResponse getPassageById(String passageId) {
        Passage passage = passageRepository.findById(passageId)
                .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
        return mapToResponse(passage);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private PassageResponse mapToResponse(Passage passage) {
        return PassageResponse.builder()
                .id(passage.getId())
                .content(passage.getContent())
                .imageUrl(passage.getImageUrl())
                .createdAt(passage.getCreatedAt())
                .build();
    }
}
