package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.ExamRequest;
import kaito.jlpt.ktjlpt.dto.response.*;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamService {

    ExamRepository examRepository;
    ExamSectionRepository examSectionRepository;
    QuestionGroupRepository questionGroupRepository;
    QuestionRepository questionRepository;
    QuestionOptionRepository questionOptionRepository;

    // ─────────────────────────────────────────────────────────────────
    // ADMIN operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * Tạo mới một Exam. Chỉ ADMIN mới gọi (kiểm tra ở Controller).
     */
    public ExamResponse createExam(ExamRequest request) {
        log.info("createExam - title: {}, level: {}", request.getTitle(), request.getLevel());
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .level(request.getLevel())
                .totalTime(request.getTotalTime())
                .totalScore(request.getTotalScore() != null ? request.getTotalScore() : 180)
                .passScore(request.getPassScore() != null ? request.getPassScore() : 90)
                .build();
        return mapToExamResponse(examRepository.save(exam));
    }

    /**
     * Cập nhật thông tin Exam. Chỉ ADMIN mới gọi (kiểm tra ở Controller).
     */
    public ExamResponse updateExam(String examId, ExamRequest request) {
        log.info("updateExam - examId: {}", examId);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));
        exam.setTitle(request.getTitle());
        exam.setLevel(request.getLevel());
        exam.setTotalTime(request.getTotalTime());
        if (request.getTotalScore() != null) exam.setTotalScore(request.getTotalScore());
        if (request.getPassScore() != null) exam.setPassScore(request.getPassScore());
        return mapToExamResponse(examRepository.save(exam));
    }

    /**
     * Xóa Exam. Chỉ ADMIN mới gọi (kiểm tra ở Controller).
     */
    public void deleteExam(String examId) {
        log.info("deleteExam - examId: {}", examId);
        if (!examRepository.existsById(examId)) {
            throw new AppException(ErrorCode.EXAM_NOT_FOUND);
        }
        examRepository.deleteById(examId);
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC / AUTHENTICATED operations
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lấy danh sách tất cả Exam.
     */
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::mapToExamResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin cơ bản của một Exam theo ID.
     */
    public ExamResponse getExamById(String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));
        return mapToExamResponse(exam);
    }

    /**
     * Lọc Exam theo level.
     */
    public List<ExamResponse> getExamsByLevel(String level) {
        return examRepository.findByLevelOrderByCreatedAtDesc(level).stream()
                .map(this::mapToExamResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy toàn bộ cấu trúc Exam: sections → groups → questions → options.
     * Nếu caller không có role ADMIN thì isCorrect của options sẽ bị ẩn (null).
     */
    public ExamWithSectionsResponse getExamWithFullContent(String examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        boolean isAdmin = isCurrentUserAdmin();

        List<ExamSection> sections = examSectionRepository.findByExamIdOrderBySectionOrder(examId);
        List<ExamSectionWithGroupsResponse> sectionResponses = sections.stream()
                .map(section -> buildSectionWithGroups(section, isAdmin))
                .collect(Collectors.toList());

        return ExamWithSectionsResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel())
                .totalTime(exam.getTotalTime())
                .totalScore(exam.getTotalScore())
                .passScore(exam.getPassScore())
                .sectionCount(sections.size())
                .sections(sectionResponses)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private ExamSectionWithGroupsResponse buildSectionWithGroups(ExamSection section, boolean isAdmin) {
        List<QuestionGroup> groups =
                questionGroupRepository.findByExamSectionIdOrderByGroupOrder(section.getId());
        List<QuestionGroupWithQuestionsResponse> groupResponses = groups.stream()
                .map(group -> buildGroupWithQuestions(group, isAdmin))
                .collect(Collectors.toList());

        return ExamSectionWithGroupsResponse.builder()
                .id(section.getId())
                .examId(section.getExam() != null ? section.getExam().getId() : null)
                .title(section.getTitle())
                .sectionType(section.getSectionType())
                .sectionOrder(section.getSectionOrder())
                .timeLimit(section.getTimeLimit())
                .audioUrl(section.getAudioUrl())
                .createdAt(section.getCreatedAt())
                .groups(groupResponses)
                .build();
    }

    private QuestionGroupWithQuestionsResponse buildGroupWithQuestions(QuestionGroup group, boolean isAdmin) {
        List<Question> questions =
                questionRepository.findByQuestionGroupIdOrderByQuestionOrder(group.getId());
        List<QuestionResponse> questionResponses = questions.stream()
                .map(q -> buildQuestionWithOptions(q, isAdmin))
                .collect(Collectors.toList());

        return QuestionGroupWithQuestionsResponse.builder()
                .id(group.getId())
                .sectionId(group.getExamSection() != null ? group.getExamSection().getId() : null)
                .passageId(group.getPassage() != null ? group.getPassage().getId() : null)
                .title(group.getTitle())
                .groupOrder(group.getGroupOrder())
                .description(group.getDescription())
                .createdAt(group.getCreatedAt())
                .questions(questionResponses)
                .build();
    }

    private QuestionResponse buildQuestionWithOptions(Question question, boolean isAdmin) {
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

    /**
     * Map QuestionOption → DTO. Nếu isAdmin = false, set isCorrect = null để ẩn đáp án.
     */
    QuestionOptionResponse mapToOptionResponse(QuestionOption opt, boolean isAdmin) {
        return QuestionOptionResponse.builder()
                .id(opt.getId())
                .questionId(opt.getQuestion() != null ? opt.getQuestion().getId() : null)
                .content(opt.getContent())
                .optionOrder(opt.getOptionOrder())
                .isCorrect(isAdmin ? opt.getIsCorrect() : null)
                .createdAt(opt.getCreatedAt())
                .build();
    }

    private ExamResponse mapToExamResponse(Exam exam) {
        List<ExamSection> sections = exam.getSections();
        int sectionCount = (sections != null) ? sections.size() : 0;
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel())
                .totalTime(exam.getTotalTime())
                .totalScore(exam.getTotalScore())
                .passScore(exam.getPassScore())
                .createdAt(exam.getCreatedAt())
                .sectionCount(sectionCount)
                .build();
    }

    /**
     * Kiểm tra xem người dùng hiện tại có role ADMIN không.
     */
    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
