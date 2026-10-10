package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.StartAttemptRequest;
import kaito.jlpt.ktjlpt.dto.request.SubmitAnswerRequest;
import kaito.jlpt.ktjlpt.dto.request.SubmitAttemptRequest;
import kaito.jlpt.ktjlpt.dto.response.AttemptResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamReviewResponse;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.AttemptMode;
import kaito.jlpt.ktjlpt.enums.AttemptScope;
import kaito.jlpt.ktjlpt.enums.AttemptStatus;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttemptService {

    ExamRepository examRepository;
    SectionRepository sectionRepository;
    QuestionRepository questionRepository;
    QuestionOptionRepository questionOptionRepository;
    ExamAttemptRepository examAttemptRepository;
    AttemptAnswerRepository attemptAnswerRepository;
    AttemptSectionRepository attemptSectionRepository;
    UserRepository userRepository;

    // ─── Bắt đầu lượt làm bài ────────────────────────────────────────────────

    @Transactional
    public AttemptResponse startAttempt(String userId, String examId, StartAttemptRequest request) {
        // Validate scope vs sectionId
        if (request.getScope() == AttemptScope.SECTION && request.getSectionId() == null) {
            throw new AppException(ErrorCode.SECTION_REQUIRED_FOR_SCOPE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        if (!exam.getPublished()) {
            throw new AppException(ErrorCode.EXAM_NOT_PUBLISHED);
        }

        // Check duplicate in-progress (chỉ áp dụng với MOCK_TEST)
        if (request.getMode() == AttemptMode.MOCK_TEST) {
            Optional<ExamAttempt> existing = examAttemptRepository
                    .findByUserIdAndExamIdAndStatus(userId, examId, AttemptStatus.IN_PROGRESS);
            if (existing.isPresent()) {
                throw new AppException(ErrorCode.ATTEMPT_ALREADY_IN_PROGRESS);
            }
        }

        Section section = null;
        if (request.getScope() == AttemptScope.SECTION) {
            section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new AppException(ErrorCode.SECTION_NOT_FOUND));
        }

        Instant now = Instant.now();
        Integer durationMinutes = request.getMode() == AttemptMode.MOCK_TEST
                ? (request.getScope() == AttemptScope.FULL ? exam.getDurationMinutes() : null)
                : null;

        Instant expiresAt = (durationMinutes != null)
                ? now.plusSeconds(durationMinutes * 60L)
                : null;

        // Đếm số câu hỏi trong phạm vi
        long totalQuestions;
        if (request.getScope() == AttemptScope.FULL) {
            totalQuestions = questionRepository.countByExamId(examId);
        } else {
            totalQuestions = questionRepository.countByExamIdAndSectionId(request.getSectionId());
        }

        ExamAttempt attempt = ExamAttempt.builder()
                .user(user)
                .exam(exam)
                .mode(request.getMode())
                .scope(request.getScope())
                .section(section)
                .status(AttemptStatus.IN_PROGRESS)
                .durationMinutes(durationMinutes)
                .startedAt(now)
                .expiresAt(expiresAt)
                .totalQuestions((int) totalQuestions)
                .build();

        attempt = examAttemptRepository.save(attempt);
        return toAttemptResponse(attempt);
    }

    // ─── Lưu một câu trả lời trong khi làm bài ──────────────────────────────

    @Transactional
    public void saveAnswer(String userId, String attemptId, SubmitAnswerRequest request) {
        ExamAttempt attempt = getValidAttempt(userId, attemptId);

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        QuestionOption selectedOption = null;
        if (request.getSelectedOptionId() != null) {
            selectedOption = questionOptionRepository.findById(request.getSelectedOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));
        }

        // Upsert: tìm answer cũ hoặc tạo mới
        AttemptAnswer answer = attemptAnswerRepository
                .findByAttemptIdAndQuestionId(attemptId, request.getQuestionId())
                .orElse(AttemptAnswer.builder()
                        .attempt(attempt)
                        .question(question)
                        .build());

        answer.setSelectedOption(selectedOption);
        answer.setAnsweredAt(Instant.now());
        if (request.getFlagged() != null) {
            answer.setFlagged(request.getFlagged());
        }

        // Trong PRACTICE mode: chấm điểm ngay
        if (attempt.getMode() == AttemptMode.PRACTICE && selectedOption != null) {
            answer.setCorrect(selectedOption.getCorrect());
        }

        attemptAnswerRepository.save(answer);
    }

    // ─── Nộp toàn bộ bài thi ─────────────────────────────────────────────────

    @Transactional
    public AttemptResponse submitAttempt(String userId, String attemptId, SubmitAttemptRequest request) {
        ExamAttempt attempt = getValidAttempt(userId, attemptId);

        // Lưu tất cả đáp án
        for (SubmitAnswerRequest answerReq : request.getAnswers()) {
            saveAnswer(userId, attemptId, answerReq);
        }

        // Chấm điểm toàn bộ
        List<AttemptAnswer> allAnswers = attemptAnswerRepository.findByAttemptId(attemptId);
        int correctCount = 0;
        for (AttemptAnswer ans : allAnswers) {
            if (ans.getSelectedOption() != null) {
                boolean correct = Boolean.TRUE.equals(ans.getSelectedOption().getCorrect());
                ans.setCorrect(correct);
                if (correct) correctCount++;
            } else {
                ans.setCorrect(false);
            }
            attemptAnswerRepository.save(ans);
        }

        // Cập nhật attempt
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(Instant.now());
        attempt.setCorrectCount(correctCount);
        if (request.getTimeSpentSeconds() != null) {
            attempt.setTimeSpentSeconds(request.getTimeSpentSeconds());
        }

        attempt = examAttemptRepository.save(attempt);
        return toAttemptResponse(attempt);
    }

    // ─── Lấy danh sách attempt của user ─────────────────────────────────────

    public List<AttemptResponse> getUserAttempts(String userId) {
        return examAttemptRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toAttemptResponse)
                .collect(Collectors.toList());
    }

    // ─── Lấy kết quả và review sau khi nộp ──────────────────────────────────

    @Transactional(readOnly = true)
    public ExamReviewResponse getReview(String userId, String attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTEMPT_NOT_FOUND));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.ATTEMPT_NOT_OWNED);
        }

        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.ATTEMPT_ALREADY_IN_PROGRESS);
        }

        // Lấy map answerByQuestionId để tra cứu O(1)
        Map<String, AttemptAnswer> answerMap = attemptAnswerRepository
                .findByAttemptId(attemptId)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a
                ));

        Exam exam = attempt.getExam();
        List<ExamReviewResponse.SectionReviewResponse> sections =
                sectionRepository.findByExamIdOrderByOrderNoAsc(exam.getId())
                        .stream()
                        .map(s -> toSectionReview(s, answerMap))
                        .collect(Collectors.toList());

        return ExamReviewResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel().name())
                .sections(sections)
                .build();
    }

    // ─── Helper: validate attempt ─────────────────────────────────────────────

    private ExamAttempt getValidAttempt(String userId, String attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.ATTEMPT_NOT_FOUND));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.ATTEMPT_NOT_OWNED);
        }

        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            throw new AppException(ErrorCode.ATTEMPT_ALREADY_SUBMITTED);
        }

        if (attempt.getStatus() == AttemptStatus.EXPIRED) {
            throw new AppException(ErrorCode.ATTEMPT_EXPIRED);
        }

        // Kiểm tra hết hạn giờ
        if (attempt.getExpiresAt() != null && Instant.now().isAfter(attempt.getExpiresAt())) {
            attempt.setStatus(AttemptStatus.EXPIRED);
            examAttemptRepository.save(attempt);
            throw new AppException(ErrorCode.ATTEMPT_EXPIRED);
        }

        return attempt;
    }

    // ─── Mapping helpers cho review ──────────────────────────────────────────

    private ExamReviewResponse.SectionReviewResponse toSectionReview(
            Section section, Map<String, AttemptAnswer> answerMap) {

        List<ExamReviewResponse.MondaiReviewResponse> mondaiList = section.getMondaiList()
                .stream()
                .map(m -> toMondaiReview(m, answerMap))
                .collect(Collectors.toList());

        return ExamReviewResponse.SectionReviewResponse.builder()
                .id(section.getId())
                .sectionType(section.getSectionType())
                .title(section.getTitle())
                .orderNo(section.getOrderNo())
                .audioUrl(section.getAudioUrl())
                .mondaiList(mondaiList)
                .build();
    }

    private ExamReviewResponse.MondaiReviewResponse toMondaiReview(
            Mondai mondai, Map<String, AttemptAnswer> answerMap) {

        List<ExamReviewResponse.QuestionGroupReviewResponse> groups = mondai.getQuestionGroups()
                .stream()
                .map(g -> toGroupReview(g, answerMap))
                .collect(Collectors.toList());

        return ExamReviewResponse.MondaiReviewResponse.builder()
                .id(mondai.getId())
                .mondaiNo(mondai.getMondaiNo())
                .orderNo(mondai.getOrderNo())
                .instruction(mondai.getInstruction())
                .questionGroups(groups)
                .build();
    }

    private ExamReviewResponse.QuestionGroupReviewResponse toGroupReview(
            QuestionGroup group, Map<String, AttemptAnswer> answerMap) {

        List<ExamReviewResponse.QuestionReviewResponse> questions = group.getQuestions()
                .stream()
                .map(q -> toQuestionReview(q, answerMap.get(q.getId())))
                .collect(Collectors.toList());

        return ExamReviewResponse.QuestionGroupReviewResponse.builder()
                .id(group.getId())
                .orderNo(group.getOrderNo())
                .passage(group.getPassage())
                .imageUrl(group.getImageUrl())
                .transcript(group.getTranscript()) // Trả ra sau submit
                .questions(questions)
                .build();
    }

    private ExamReviewResponse.QuestionReviewResponse toQuestionReview(
            Question question, AttemptAnswer answer) {

        List<ExamReviewResponse.OptionReviewResponse> options = question.getOptions()
                .stream()
                .map(o -> ExamReviewResponse.OptionReviewResponse.builder()
                        .id(o.getId())
                        .optionNo(o.getOptionNo())
                        .optionText(o.getOptionText())
                        .imageUrl(o.getImageUrl())
                        .isCorrect(o.getCorrect())
                        .build())
                .collect(Collectors.toList());

        return ExamReviewResponse.QuestionReviewResponse.builder()
                .id(question.getId())
                .orderNo(question.getOrderNo())
                .questionNo(question.getQuestionNo())
                .content(question.getContent())
                .explanation(question.getExplanation())
                .selectedOptionId(answer != null && answer.getSelectedOption() != null
                        ? answer.getSelectedOption().getId() : null)
                .correct(answer != null ? answer.getCorrect() : null)
                .flagged(answer != null ? answer.getFlagged() : false)
                .options(options)
                .build();
    }

    // ─── Mapping attempt → response ──────────────────────────────────────────

    private AttemptResponse toAttemptResponse(ExamAttempt attempt) {
        return AttemptResponse.builder()
                .id(attempt.getId())
                .examId(attempt.getExam().getId())
                .examTitle(attempt.getExam().getTitle())
                .examLevel(attempt.getExam().getLevel())
                .mode(attempt.getMode())
                .scope(attempt.getScope())
                .sectionId(attempt.getSection() != null ? attempt.getSection().getId() : null)
                .sectionType(attempt.getSection() != null ? attempt.getSection().getSectionType() : null)
                .status(attempt.getStatus())
                .durationMinutes(attempt.getDurationMinutes())
                .startedAt(attempt.getStartedAt())
                .expiresAt(attempt.getExpiresAt())
                .submittedAt(attempt.getSubmittedAt())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .correctCount(attempt.getCorrectCount())
                .totalQuestions(attempt.getTotalQuestions())
                .build();
    }
}
