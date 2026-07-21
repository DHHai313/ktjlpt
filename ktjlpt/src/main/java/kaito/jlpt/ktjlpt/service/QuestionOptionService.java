package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.QuestionOptionRequest;
import kaito.jlpt.ktjlpt.dto.response.QuestionOptionResponse;
import kaito.jlpt.ktjlpt.entity.Question;
import kaito.jlpt.ktjlpt.entity.QuestionOption;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
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
public class QuestionOptionService {

    QuestionOptionRepository questionOptionRepository;
    QuestionRepository questionRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    public QuestionOptionResponse createOption(QuestionOptionRequest request) {
        log.info("createOption - questionId: {}, order: {}", request.getQuestionId(), request.getOptionOrder());
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        QuestionOption option = QuestionOption.builder()
                .question(question)
                .content(request.getContent())
                .optionOrder(request.getOptionOrder())
                .isCorrect(request.getIsCorrect() != null ? request.getIsCorrect() : false)
                .build();
        return mapToResponse(questionOptionRepository.save(option), true);
    }

    public QuestionOptionResponse updateOption(String optionId, QuestionOptionRequest request) {
        log.info("updateOption - optionId: {}", optionId);
        QuestionOption option = questionOptionRepository.findById(optionId)
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_OPTION_NOT_FOUND));

        if (!option.getQuestion().getId().equals(request.getQuestionId())) {
            Question question = questionRepository.findById(request.getQuestionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
            option.setQuestion(question);
        }
        option.setContent(request.getContent());
        option.setOptionOrder(request.getOptionOrder());
        option.setIsCorrect(request.getIsCorrect() != null ? request.getIsCorrect() : false);
        return mapToResponse(questionOptionRepository.save(option), true);
    }

    public void deleteOption(String optionId) {
        log.info("deleteOption - optionId: {}", optionId);
        if (!questionOptionRepository.existsById(optionId)) {
            throw new AppException(ErrorCode.QUESTION_OPTION_NOT_FOUND);
        }
        questionOptionRepository.deleteById(optionId);
    }

    /**
     * ADMIN: lấy danh sách options của câu hỏi, có trả về isCorrect.
     */
    public List<QuestionOptionResponse> getOptionsByQuestionIdAdmin(String questionId) {
        return questionOptionRepository.findByQuestionIdOrderByOptionOrder(questionId).stream()
                .map(opt -> mapToResponse(opt, true))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * USER/PUBLIC: lấy danh sách options của câu hỏi, KHÔNG trả về isCorrect.
     */
    public List<QuestionOptionResponse> getOptionsByQuestionId(String questionId) {
        boolean isAdmin = isCurrentUserAdmin();
        return questionOptionRepository.findByQuestionIdOrderByOptionOrder(questionId).stream()
                .map(opt -> mapToResponse(opt, isAdmin))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private QuestionOptionResponse mapToResponse(QuestionOption opt, boolean isAdmin) {
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
