package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.QuestionGroupRequest;
import kaito.jlpt.ktjlpt.dto.response.QuestionGroupResponse;
import kaito.jlpt.ktjlpt.entity.ExamSection;
import kaito.jlpt.ktjlpt.entity.Passage;
import kaito.jlpt.ktjlpt.entity.QuestionGroup;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.ExamSectionRepository;
import kaito.jlpt.ktjlpt.repository.PassageRepository;
import kaito.jlpt.ktjlpt.repository.QuestionGroupRepository;
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
public class QuestionGroupService {

    QuestionGroupRepository questionGroupRepository;
    ExamSectionRepository examSectionRepository;
    PassageRepository passageRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    public QuestionGroupResponse createGroup(QuestionGroupRequest request) {
        log.info("createGroup - sectionId: {}, order: {}", request.getSectionId(), request.getGroupOrder());
        ExamSection section = examSectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_SECTION_NOT_FOUND));

        Passage passage = null;
        if (request.getPassageId() != null && !request.getPassageId().isBlank()) {
            passage = passageRepository.findById(request.getPassageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
        }

        QuestionGroup group = QuestionGroup.builder()
                .examSection(section)
                .passage(passage)
                .title(request.getTitle())
                .groupOrder(request.getGroupOrder())
                .description(request.getDescription())
                .build();
        return mapToResponse(questionGroupRepository.save(group));
    }

    public QuestionGroupResponse updateGroup(String groupId, QuestionGroupRequest request) {
        log.info("updateGroup - groupId: {}", groupId);
        QuestionGroup group = questionGroupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        if (!group.getExamSection().getId().equals(request.getSectionId())) {
            ExamSection section = examSectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXAM_SECTION_NOT_FOUND));
            group.setExamSection(section);
        }

        if (request.getPassageId() != null && !request.getPassageId().isBlank()) {
            Passage passage = passageRepository.findById(request.getPassageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
            group.setPassage(passage);
        } else {
            group.setPassage(null);
        }

        group.setTitle(request.getTitle());
        group.setGroupOrder(request.getGroupOrder());
        group.setDescription(request.getDescription());
        return mapToResponse(questionGroupRepository.save(group));
    }

    public void deleteGroup(String groupId) {
        log.info("deleteGroup - groupId: {}", groupId);
        if (!questionGroupRepository.existsById(groupId)) {
            throw new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND);
        }
        questionGroupRepository.deleteById(groupId);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    public List<QuestionGroupResponse> getGroupsBySectionId(String sectionId) {
        return questionGroupRepository.findByExamSectionIdOrderByGroupOrder(sectionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public QuestionGroupResponse getGroupById(String groupId) {
        QuestionGroup group = questionGroupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));
        return mapToResponse(group);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private QuestionGroupResponse mapToResponse(QuestionGroup group) {
        return QuestionGroupResponse.builder()
                .id(group.getId())
                .sectionId(group.getExamSection() != null ? group.getExamSection().getId() : null)
                .passageId(group.getPassage() != null ? group.getPassage().getId() : null)
                .title(group.getTitle())
                .groupOrder(group.getGroupOrder())
                .description(group.getDescription())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
