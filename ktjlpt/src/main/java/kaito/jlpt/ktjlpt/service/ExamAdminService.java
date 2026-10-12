package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.request.ExamUpdateRequest;
import kaito.jlpt.ktjlpt.dto.response.ExamAdminDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamAdminSummaryResponse;
import kaito.jlpt.ktjlpt.dto.response.PageResponse;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.enums.SectionType;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.mapper.ExamMapper;
import kaito.jlpt.ktjlpt.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamAdminService {

    ExamRepository examRepository;
    SectionRepository sectionRepository;
    MondaiRepository mondaiRepository;
    QuestionGroupRepository questionGroupRepository;
    QuestionRepository questionRepository;
    QuestionOptionRepository questionOptionRepository;
    ExamAttemptRepository examAttemptRepository;
    ExamMapper examMapper;

    /**
     * 1. GET /admin/exams
     * Lấy danh sách tất cả đề thi có phân trang, lọc theo level, published và tìm kiếm theo keyword.
     */
    @Transactional(readOnly = true)
    public PageResponse<ExamAdminSummaryResponse> getExams(
            JlptLevel level,
            Boolean published,
            String keyword,
            Pageable pageable) {

        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ExamAdminSummaryResponse> page = examRepository.findAllAdmin(level, published, kw, effectivePageable);
        return PageResponse.from(page);
    }

    /**
     * 2. GET /admin/exams/{id}
     * Lấy chi tiết đề thi đầy đủ thông tin (kèm đáp án đúng) cho Admin.
     * Sử dụng 5 bước truy vấn IN để tránh N+1 và tránh MultipleBagFetchException.
     */
    @Transactional(readOnly = true)
    public ExamAdminDetailResponse getExamDetail(String id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        List<Section> sections = sectionRepository.findByExamIdOrderByOrderNoAsc(exam.getId());

        List<String> sectionIds = sections.stream().map(Section::getId).toList();
        List<Mondai> mondaiList = sectionIds.isEmpty()
                ? List.of()
                : mondaiRepository.findBySectionIdInOrderByOrderNoAsc(sectionIds);

        List<String> mondaiIds = mondaiList.stream().map(Mondai::getId).toList();
        List<QuestionGroup> groups = mondaiIds.isEmpty()
                ? List.of()
                : questionGroupRepository.findByMondaiIdInOrderByOrderNoAsc(mondaiIds);

        List<String> groupIds = groups.stream().map(QuestionGroup::getId).toList();
        List<Question> questions = groupIds.isEmpty()
                ? List.of()
                : questionRepository.findByGroupIdInOrderByOrderNoAsc(groupIds);

        List<String> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionOption> options = questionIds.isEmpty()
                ? List.of()
                : questionOptionRepository.findByQuestionIdInOrderByOptionNoAsc(questionIds);

        // Gom nhóm cấp con theo ID cấp cha
        Map<String, List<QuestionOption>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestion().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<String, List<Question>> questionsByGroup = questions.stream()
                .collect(Collectors.groupingBy(q -> q.getGroup().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<String, List<QuestionGroup>> groupsByMondai = groups.stream()
                .collect(Collectors.groupingBy(g -> g.getMondai().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<String, List<Mondai>> mondaisBySection = mondaiList.stream()
                .collect(Collectors.groupingBy(m -> m.getSection().getId(), LinkedHashMap::new, Collectors.toList()));

        // Ghép cây DTO
        List<ExamAdminDetailResponse.SectionAdminResponse> sectionResponses = sections.stream().map(section -> {
            List<Mondai> sMondais = mondaisBySection.getOrDefault(section.getId(), List.of());

            List<ExamAdminDetailResponse.MondaiAdminResponse> mondaiResponses = sMondais.stream().map(mondai -> {
                List<QuestionGroup> mGroups = groupsByMondai.getOrDefault(mondai.getId(), List.of());

                List<ExamAdminDetailResponse.QuestionGroupAdminResponse> groupResponses = mGroups.stream().map(group -> {
                    List<Question> gQuestions = questionsByGroup.getOrDefault(group.getId(), List.of());

                    List<ExamAdminDetailResponse.QuestionAdminResponse> questionResponses = gQuestions.stream().map(question -> {
                        List<QuestionOption> qOptions = optionsByQuestion.getOrDefault(question.getId(), List.of());

                        List<ExamAdminDetailResponse.OptionAdminResponse> optionResponses = qOptions.stream().map(option ->
                                ExamAdminDetailResponse.OptionAdminResponse.builder()
                                        .id(option.getId())
                                        .optionNo(option.getOptionNo())
                                        .optionText(option.getOptionText())
                                        .imageUrl(option.getImageUrl())
                                        .correct(option.getCorrect())
                                        .build()
                        ).toList();

                        return ExamAdminDetailResponse.QuestionAdminResponse.builder()
                                .id(question.getId())
                                .orderNo(question.getOrderNo())
                                .questionNo(question.getQuestionNo())
                                .content(question.getContent())
                                .explanation(question.getExplanation())
                                .options(optionResponses)
                                .build();
                    }).toList();

                    return ExamAdminDetailResponse.QuestionGroupAdminResponse.builder()
                            .id(group.getId())
                            .orderNo(group.getOrderNo())
                            .passage(group.getPassage())
                            .imageUrl(group.getImageUrl())
                            .transcript(group.getTranscript())
                            .questions(questionResponses)
                            .build();
                }).toList();

                return ExamAdminDetailResponse.MondaiAdminResponse.builder()
                        .id(mondai.getId())
                        .mondaiNo(mondai.getMondaiNo())
                        .orderNo(mondai.getOrderNo())
                        .instruction(mondai.getInstruction())
                        .questionGroups(groupResponses)
                        .build();
            }).toList();

            return ExamAdminDetailResponse.SectionAdminResponse.builder()
                    .id(section.getId())
                    .sectionType(section.getSectionType())
                    .title(section.getTitle())
                    .orderNo(section.getOrderNo())
                    .audioUrl(section.getAudioUrl())
                    .mondaiList(mondaiResponses)
                    .build();
        }).toList();

        return ExamAdminDetailResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel())
                .durationMinutes(exam.getDurationMinutes())
                .published(exam.getPublished())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .sections(sectionResponses)
                .build();
    }

    /**
     * 3a. PATCH /admin/exams/{id}/publish
     * Xuất bản đề thi sau khi kiểm tra hợp lệ toàn bộ cấu trúc.
     */
    @Transactional
    public ExamAdminSummaryResponse publishExam(String id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        // Idempotent: nếu đã publish rồi thì trả về luôn
        if (Boolean.TRUE.equals(exam.getPublished())) {
            return toSummary(exam);
        }

        // Validate tính hợp lệ trước khi publish
        validatePublishable(exam);

        exam.setPublished(true);
        exam = examRepository.save(exam);

        return toSummary(exam);
    }

    /**
     * 3b. PATCH /admin/exams/{id}/unpublish
     * Hủy xuất bản đề thi.
     */
    @Transactional
    public ExamAdminSummaryResponse unpublishExam(String id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        // Idempotent: nếu đã unpublish rồi thì trả về luôn
        if (!Boolean.TRUE.equals(exam.getPublished())) {
            return toSummary(exam);
        }

        exam.setPublished(false);
        exam = examRepository.save(exam);

        return toSummary(exam);
    }

    /**
     * 4. PUT /admin/exams/{id}
     * Cập nhật thông tin cơ bản của đề thi (title, level, durationMinutes).
     */
    @Transactional
    public ExamAdminSummaryResponse updateExam(String id, ExamUpdateRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        examMapper.updateExam(exam, request);
        exam = examRepository.save(exam);

        return toSummary(exam);
    }

    /**
     * 5. DELETE /admin/exams/{id}
     * Xóa đề thi (tự động cascade các phần con). Nếu đã có lượt thi thì từ chối.
     */
    @Transactional
    public void deleteExam(String id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        if (examAttemptRepository.existsByExamId(id)) {
            throw new AppException(ErrorCode.EXAM_HAS_ATTEMPTS,
                    "Đề thi đã có lượt làm bài, không thể xóa. Bạn có thể hủy xuất bản (unpublish) đề thi thay vì xóa.");
        }

        examRepository.delete(exam);
    }

    /**
     * Kiểm tra điều kiện xuất bản đề thi:
     * 1. Có ít nhất 1 section và 1 câu hỏi.
     * 2. Mọi câu hỏi có ít nhất 2 option và đúng 1 option correct = true.
     * 3. Section LISTENING bắt buộc phải có audioUrl.
     */
    private void validatePublishable(Exam exam) {
        List<Section> sections = sectionRepository.findByExamIdOrderByOrderNoAsc(exam.getId());
        if (sections.isEmpty()) {
            throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                    "Đề thi không thể xuất bản: Đề thi phải có ít nhất 1 section.");
        }

        // Kiểm tra section LISTENING phải có audioUrl
        for (Section section : sections) {
            if (section.getSectionType() == SectionType.LISTENING
                    && (section.getAudioUrl() == null || section.getAudioUrl().isBlank())) {
                throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                        "Đề thi không thể xuất bản: Phần thi LISTENING ("
                                + (section.getTitle() != null ? section.getTitle() : "Listening")
                                + ") bắt buộc phải có audioUrl.");
            }
        }

        List<Question> questions = questionRepository.findByExamIdOrderByQuestionNoAsc(exam.getId());
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                    "Đề thi không thể xuất bản: Đề thi phải có ít nhất 1 câu hỏi.");
        }

        List<String> questionIds = questions.stream().map(Question::getId).toList();
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdInOrderByOptionNoAsc(questionIds);
        Map<String, List<QuestionOption>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestion().getId()));

        for (Question q : questions) {
            List<QuestionOption> qOptions = optionsByQuestion.getOrDefault(q.getId(), List.of());
            if (qOptions.size() < 2) {
                throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                        "Đề thi không thể xuất bản: Câu hỏi số " + q.getQuestionNo()
                                + " phải có ít nhất 2 lựa chọn (hiện có " + qOptions.size() + ").");
            }

            long correctCount = qOptions.stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getCorrect()))
                    .count();

            if (correctCount == 0) {
                throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                        "Đề thi không thể xuất bản: Câu hỏi số " + q.getQuestionNo()
                                + " chưa có đáp án đúng.");
            }

            if (correctCount > 1) {
                throw new AppException(ErrorCode.EXAM_NOT_PUBLISHABLE,
                        "Đề thi không thể xuất bản: Câu hỏi số " + q.getQuestionNo()
                                + " có nhiều hơn 1 đáp án đúng (" + correctCount + " đáp án đúng).");
            }
        }
    }

    /**
     * Chuyển đổi Entity Exam thành ExamAdminSummaryResponse kèm tổng số section và câu hỏi.
     */
    private ExamAdminSummaryResponse toSummary(Exam exam) {
        long totalSections = sectionRepository.countByExamId(exam.getId());
        long totalQuestions = questionRepository.countByExamId(exam.getId());

        return ExamAdminSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel())
                .durationMinutes(exam.getDurationMinutes())
                .published(exam.getPublished())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .totalSections(totalSections)
                .totalQuestions(totalQuestions)
                .build();
    }
}
