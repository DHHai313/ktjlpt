package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.SubmitAnswerRequest;
import kaito.jlpt.ktjlpt.dto.response.AttemptDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.AttemptResponse;
import kaito.jlpt.ktjlpt.dto.response.UserAnswerResponse;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttemptService {

    UserExamAttemptsRepository attemptsRepository;
    UserAnswerRepository userAnswerRepository;
    ExamRepository examRepository;
    UserRepository userRepository;
    QuestionRepository questionRepository;
    QuestionOptionRepository questionOptionRepository;

    // ─────────────────────────────────────────────────────────────────
    // USER operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * Bắt đầu một lần thi mới. Status mặc định = "IN_PROGRESS".
     *
     * @param examId    ID của đề thi
     * @param userEmail email lấy từ SecurityContext
     */
    @Transactional
    public AttemptResponse startAttempt(String examId, String userEmail) {
        log.info("startAttempt - examId: {}, user: {}", examId, userEmail);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        UserExamAttempts attempt = UserExamAttempts.builder()
                .user(user)
                .exam(exam)
                .status("IN_PROGRESS")
                .build();
        return mapToAttemptResponse(attemptsRepository.save(attempt));
    }

    /**
     * Nộp (hoặc cập nhật) một câu trả lời cho attempt.
     * - Nếu câu hỏi đã có answer → UPDATE (không duplicate).
     * - isCorrect được tính dựa trên QuestionOption.isCorrect.
     * - Nếu selectedOptionId = null → bỏ qua, isCorrect = false.
     */
    @Transactional
    public UserAnswerResponse submitAnswer(String attemptId, SubmitAnswerRequest request, String userEmail) {
        log.info("submitAnswer - attemptId: {}, questionId: {}", attemptId, request.getQuestionId());

        UserExamAttempts attempt = getAttemptForUser(attemptId, userEmail);
        checkAttemptInProgress(attempt);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));

        // Tính isCorrect
        Boolean isCorrect = false;
        QuestionOption selectedOption = null;
        if (request.getSelectedOptionId() != null && !request.getSelectedOptionId().isBlank()) {
            selectedOption = questionOptionRepository.findById(request.getSelectedOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.QUESTION_OPTION_NOT_FOUND));
            isCorrect = Boolean.TRUE.equals(selectedOption.getIsCorrect());
        }

        // Upsert: nếu đã có answer thì update, chưa có thì insert
        UserAnswer answer = userAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, question.getId())
                .orElse(null);

        if (answer == null) {
            answer = UserAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedOption(selectedOption)
                    .isCorrect(isCorrect)
                    .build();
        } else {
            answer.setSelectedOption(selectedOption);
            answer.setIsCorrect(isCorrect);
        }

        return mapToAnswerResponse(userAnswerRepository.save(answer));
    }

    /**
     * Nộp nhiều câu trả lời một lúc (bulk submit).
     */
    @Transactional
    public List<UserAnswerResponse> bulkSubmitAnswers(String attemptId,
                                                      List<SubmitAnswerRequest> requests,
                                                      String userEmail) {
        log.info("bulkSubmitAnswers - attemptId: {}, count: {}", attemptId, requests.size());
        return requests.stream()
                .map(req -> submitAnswer(attemptId, req, userEmail))
                .collect(Collectors.toList());
    }

    /**
     * Hoàn thành lần thi:
     * - Set status = "COMPLETED", completedAt = now().
     * - Tính totalScore = số câu trả lời đúng (isCorrect = true).
     */
    @Transactional
    public AttemptResponse completeAttempt(String attemptId, String userEmail) {
        log.info("completeAttempt - attemptId: {}, user: {}", attemptId, userEmail);
        UserExamAttempts attempt = getAttemptForUser(attemptId, userEmail);
        checkAttemptInProgress(attempt);

        // Tính tổng điểm
        List<UserAnswer> answers = userAnswerRepository.findByAttemptId(attemptId);
        int score = (int) answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();

        attempt.setStatus("COMPLETED");
        attempt.setCompletedAt(Instant.now());
        attempt.setTotalScore(score);

        return mapToAttemptResponse(attemptsRepository.save(attempt));
    }

    /**
     * Lấy danh sách các lần thi của user hiện tại.
     */
    public List<AttemptResponse> getMyAttempts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return attemptsRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .map(this::mapToAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xem chi tiết một lần thi (kèm danh sách câu trả lời). Chỉ xem của chính mình.
     */
    public AttemptDetailResponse getAttemptDetail(String attemptId, String userEmail) {
        UserExamAttempts attempt = getAttemptForUser(attemptId, userEmail);
        List<UserAnswerResponse> answers = userAnswerRepository.findByAttemptId(attemptId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());

        return AttemptDetailResponse.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .examId(attempt.getExam().getId())
                .totalScore(attempt.getTotalScore())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .status(attempt.getStatus())
                .answers(answers)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * ADMIN: xem tất cả các lần thi của mọi user.
     */
    public List<AttemptResponse> getAllAttempts() {
        log.info("getAllAttempts - called by ADMIN");
        return attemptsRepository.findAll().stream()
                .map(this::mapToAttemptResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lấy attempt và kiểm tra attempt thuộc về user đang đăng nhập.
     */
    private UserExamAttempts getAttemptForUser(String attemptId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return attemptsRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ATTEMPT_NOT_FOUND));
    }

    /**
     * Kiểm tra attempt còn IN_PROGRESS (chưa hoàn thành).
     */
    private void checkAttemptInProgress(UserExamAttempts attempt) {
        if ("COMPLETED".equals(attempt.getStatus())) {
            throw new AppException(ErrorCode.ATTEMPT_ALREADY_COMPLETED);
        }
    }

    private AttemptResponse mapToAttemptResponse(UserExamAttempts attempt) {
        return AttemptResponse.builder()
                .id(attempt.getId())
                .userId(attempt.getUser() != null ? attempt.getUser().getId() : null)
                .examId(attempt.getExam() != null ? attempt.getExam().getId() : null)
                .totalScore(attempt.getTotalScore())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .status(attempt.getStatus())
                .build();
    }

    private UserAnswerResponse mapToAnswerResponse(UserAnswer answer) {
        return UserAnswerResponse.builder()
                .id(answer.getId())
                .attemptId(answer.getAttempt() != null ? answer.getAttempt().getId() : null)
                .questionId(answer.getQuestion() != null ? answer.getQuestion().getId() : null)
                .selectedOptionId(answer.getSelectedOption() != null ? answer.getSelectedOption().getId() : null)
                .isCorrect(answer.getIsCorrect())
                .build();
    }
}
