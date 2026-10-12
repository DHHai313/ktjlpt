package kaito.jlpt.ktjlpt.service;

import kaito.jlpt.ktjlpt.dto.response.ExamDetailResponse;
import kaito.jlpt.ktjlpt.dto.response.ExamSummaryResponse;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.repository.ExamRepository;
import kaito.jlpt.ktjlpt.repository.SectionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamService {

    ExamRepository examRepository;
    SectionRepository sectionRepository;

    // ─── Lấy danh sách đề thi (tóm tắt) ────────────────────────────────────

    public List<ExamSummaryResponse> getAllPublishedExams() {
        return examRepository.findByPublishedTrue()
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<ExamSummaryResponse> getPublishedExamsByLevel(JlptLevel level) {
        return examRepository.findByLevelAndPublishedTrue(level)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<ExamSummaryResponse> getExams(JlptLevel level, Boolean published) {
        List<Exam> exams;
        if (level != null && published != null) {
            exams = examRepository.findByLevelAndPublished(level, published);
        } else if (level != null) {
            exams = examRepository.findByLevel(level);
        } else if (published != null) {
            exams = examRepository.findByPublished(published);
        } else {
            exams = examRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return exams.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    // ─── Lấy chi tiết đề thi để làm bài (không có đáp án) ───────────────────

    @Transactional(readOnly = true)
    public ExamDetailResponse getExamForAttempt(String examId) {
        return getExamForAttempt(examId, false);
    }

    @Transactional(readOnly = true)
    public ExamDetailResponse getExamForAttempt(String examId, boolean isAdmin) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.EXAM_NOT_FOUND));

        if (!isAdmin && !Boolean.TRUE.equals(exam.getPublished())) {
            throw new AppException(ErrorCode.EXAM_NOT_PUBLISHED);
        }

        return toDetailResponse(exam);
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    public ExamSummaryResponse toSummary(Exam exam) {
        return ExamSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel())
                .durationMinutes(exam.getDurationMinutes())
                .published(exam.getPublished())
                .createdAt(exam.getCreatedAt())
                .build();
    }

    private ExamDetailResponse toDetailResponse(Exam exam) {
        List<ExamDetailResponse.SectionResponse> sections = sectionRepository
                .findByExamIdOrderByOrderNoAsc(exam.getId())
                .stream()
                .map(this::toSectionResponse)
                .collect(Collectors.toList());

        return ExamDetailResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .level(exam.getLevel().name())
                .durationMinutes(exam.getDurationMinutes())
                .sections(sections)
                .build();
    }

    private ExamDetailResponse.SectionResponse toSectionResponse(Section section) {
        List<ExamDetailResponse.MondaiResponse> mondaiList = section.getMondaiList()
                .stream()
                .map(this::toMondaiResponse)
                .collect(Collectors.toList());

        return ExamDetailResponse.SectionResponse.builder()
                .id(section.getId())
                .sectionType(section.getSectionType())
                .title(section.getTitle())
                .orderNo(section.getOrderNo())
                .audioUrl(section.getAudioUrl())
                .mondaiList(mondaiList)
                .build();
    }

    private ExamDetailResponse.MondaiResponse toMondaiResponse(Mondai mondai) {
        List<ExamDetailResponse.QuestionGroupResponse> groups = mondai.getQuestionGroups()
                .stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());

        return ExamDetailResponse.MondaiResponse.builder()
                .id(mondai.getId())
                .mondaiNo(mondai.getMondaiNo())
                .orderNo(mondai.getOrderNo())
                .instruction(mondai.getInstruction())
                .questionGroups(groups)
                .build();
    }

    private ExamDetailResponse.QuestionGroupResponse toGroupResponse(QuestionGroup group) {
        List<ExamDetailResponse.QuestionResponse> questions = group.getQuestions()
                .stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());

        return ExamDetailResponse.QuestionGroupResponse.builder()
                .id(group.getId())
                .orderNo(group.getOrderNo())
                .passage(group.getPassage())
                .imageUrl(group.getImageUrl())
                // transcript KHÔNG trả ở đây
                .questions(questions)
                .build();
    }

    private ExamDetailResponse.QuestionResponse toQuestionResponse(Question question) {
        List<ExamDetailResponse.OptionResponse> options = question.getOptions()
                .stream()
                .map(this::toOptionResponse)
                .collect(Collectors.toList());

        return ExamDetailResponse.QuestionResponse.builder()
                .id(question.getId())
                .orderNo(question.getOrderNo())
                .questionNo(question.getQuestionNo())
                .content(question.getContent())
                // explanation KHÔNG trả ở đây
                .options(options)
                .build();
    }

    private ExamDetailResponse.OptionResponse toOptionResponse(QuestionOption option) {
        return ExamDetailResponse.OptionResponse.builder()
                .id(option.getId())
                .optionNo(option.getOptionNo())
                .optionText(option.getOptionText())
                .imageUrl(option.getImageUrl())
                // isCorrect KHÔNG trả ở đây
                .build();
    }
}
