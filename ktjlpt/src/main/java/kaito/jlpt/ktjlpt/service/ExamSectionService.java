package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.ExamSectionRequest;
import kaito.jlpt.ktjlpt.dto.response.ExamSectionResponse;
import kaito.jlpt.ktjlpt.entity.Exam;
import kaito.jlpt.ktjlpt.entity.ExamSection;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.ExamRepository;
import kaito.jlpt.ktjlpt.repository.ExamSectionRepository;
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
public class ExamSectionService {

    ExamSectionRepository examSectionRepository;
    ExamRepository examRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    public ExamSectionResponse createSection(ExamSectionRequest request) {
        log.info("createSection - examId: {}, order: {}", request.getExamId(), request.getSectionOrder());
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        ExamSection section = ExamSection.builder()
                .exam(exam)
                .title(request.getTitle())
                .sectionType(request.getSectionType())
                .sectionOrder(request.getSectionOrder())
                .timeLimit(request.getTimeLimit())
                .audioUrl(request.getAudioUrl())
                .build();
        return mapToResponse(examSectionRepository.save(section));
    }

    public ExamSectionResponse updateSection(String sectionId, ExamSectionRequest request) {
        log.info("updateSection - sectionId: {}", sectionId);
        ExamSection section = examSectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SECTION_NOT_FOUND));

        // Nếu thay đổi examId thì load lại exam
        if (!section.getExam().getId().equals(request.getExamId())) {
            Exam exam = examRepository.findById(request.getExamId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));
            section.setExam(exam);
        }
        section.setTitle(request.getTitle());
        section.setSectionType(request.getSectionType());
        section.setSectionOrder(request.getSectionOrder());
        section.setTimeLimit(request.getTimeLimit());
        section.setAudioUrl(request.getAudioUrl());
        return mapToResponse(examSectionRepository.save(section));
    }

    public void deleteSection(String sectionId) {
        log.info("deleteSection - sectionId: {}", sectionId);
        if (!examSectionRepository.existsById(sectionId)) {
            throw new AppException(ErrorCode.EXAM_SECTION_NOT_FOUND);
        }
        examSectionRepository.deleteById(sectionId);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    public List<ExamSectionResponse> getSectionsByExamId(String examId) {
        return examSectionRepository.findByExamIdOrderBySectionOrder(examId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ExamSectionResponse getSectionById(String sectionId) {
        ExamSection section = examSectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SECTION_NOT_FOUND));
        return mapToResponse(section);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private ExamSectionResponse mapToResponse(ExamSection section) {
        return ExamSectionResponse.builder()
                .id(section.getId())
                .examId(section.getExam() != null ? section.getExam().getId() : null)
                .title(section.getTitle())
                .sectionType(section.getSectionType())
                .sectionOrder(section.getSectionOrder())
                .timeLimit(section.getTimeLimit())
                .audioUrl(section.getAudioUrl())
                .createdAt(section.getCreatedAt())
                .build();
    }
}
