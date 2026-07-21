package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.QuestionRequest;
import kaito.jlpt.ktjlpt.dto.response.QuestionOptionResponse;
import kaito.jlpt.ktjlpt.dto.response.QuestionResponse;
import kaito.jlpt.ktjlpt.entity.Passage;
import kaito.jlpt.ktjlpt.entity.Question;
import kaito.jlpt.ktjlpt.entity.QuestionGroup;
import kaito.jlpt.ktjlpt.entity.QuestionOption;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.PassageRepository;
import kaito.jlpt.ktjlpt.repository.QuestionGroupRepository;
import kaito.jlpt.ktjlpt.repository.QuestionOptionRepository;
import kaito.jlpt.ktjlpt.repository.QuestionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuestionService {

    QuestionRepository questionRepository;
    QuestionGroupRepository questionGroupRepository;
    PassageRepository passageRepository;
    QuestionOptionRepository questionOptionRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    public QuestionResponse createQuestion(QuestionRequest request) {
        log.info("createQuestion - groupId: {}, order: {}", request.getGroupId(), request.getQuestionOrder());
        QuestionGroup group = questionGroupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));

        Passage passage = null;
        if (request.getPassageId() != null && !request.getPassageId().isBlank()) {
            passage = passageRepository.findById(request.getPassageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
        }

        Question question = Question.builder()
                .questionGroup(group)
                .passage(passage)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .questionOrder(request.getQuestionOrder())
                .build();
        return mapToResponse(questionRepository.save(question), true);
    }

    public QuestionResponse updateQuestion(String questionId, QuestionRequest request) {
        log.info("updateQuestion - questionId: {}", questionId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        if (!question.getQuestionGroup().getId().equals(request.getGroupId())) {
            QuestionGroup group = questionGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_GROUP_NOT_FOUND));
            question.setQuestionGroup(group);
        }

        if (request.getPassageId() != null && !request.getPassageId().isBlank()) {
            Passage passage = passageRepository.findById(request.getPassageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PASSAGE_NOT_FOUND));
            question.setPassage(passage);
        } else {
            question.setPassage(null);
        }

        question.setContent(request.getContent());
        question.setImageUrl(request.getImageUrl());
        question.setQuestionOrder(request.getQuestionOrder());
        return mapToResponse(questionRepository.save(question), true);
    }

    public void deleteQuestion(String questionId) {
        log.info("deleteQuestion - questionId: {}", questionId);
        if (!questionRepository.existsById(questionId)) {
            throw new AppException(ErrorCode.QUESTION_NOT_FOUND);
        }
        questionRepository.deleteById(questionId);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    public List<QuestionResponse> getQuestionsByGroupId(String groupId) {
        boolean isAdmin = isCurrentUserAdmin();
        return questionRepository.findByQuestionGroupIdOrderByQuestionOrder(groupId).stream()
                .map(q -> mapToResponse(q, isAdmin))
                .collect(Collectors.toList());
    }

    public QuestionResponse getQuestionById(String questionId) {
        boolean isAdmin = isCurrentUserAdmin();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
        return mapToResponse(question, isAdmin);
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private QuestionResponse mapToResponse(Question question, boolean isAdmin) {
        List<QuestionOption> options =
                questionOptionRepository.findByQuestionIdOrderByOptionOrder(question.getId());
        List<QuestionOptionResponse> optionResponses = options.stream()
                .map(opt -> mapToOptionResponse(opt, isAdmin))
                .collect(Collectors.toList());

        return QuestionResponse.builder()
                .id(question.getId())
                .groupId(question.getQuestionGroup() != null ? question.getQuestionGroup().getId() : null)
                .passageId(question.getPassage() != null ? question.getPassage().getId() : null)
                .content(question.getContent())
                .imageUrl(question.getImageUrl())
                .questionOrder(question.getQuestionOrder())
                .createdAt(question.getCreatedAt())
                .options(optionResponses)
                .build();
    }

    private QuestionOptionResponse mapToOptionResponse(QuestionOption opt, boolean isAdmin) {
        return QuestionOptionResponse.builder()
                .id(opt.getId())
                .questionId(opt.getQuestion() != null ? opt.getQuestion().getId() : null)
                .content(opt.getContent())
                .optionOrder(opt.getOptionOrder())
                .isCorrect(isAdmin ? opt.getIsCorrect() : null)
                .createdAt(opt.getCreatedAt())
                .build();
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
