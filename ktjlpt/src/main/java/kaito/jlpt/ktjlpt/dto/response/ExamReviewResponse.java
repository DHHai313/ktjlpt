package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.SectionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * DTO trả về sau khi nộp bài (hoặc xem lại).
 * Chứa đầy đủ: is_correct, transcript, explanation.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamReviewResponse {
    String id;
    String title;
    String level;
    List<SectionReviewResponse> sections;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionReviewResponse {
        String id;
        SectionType sectionType;
        String title;
        Integer orderNo;
        String audioUrl;
        List<MondaiReviewResponse> mondaiList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MondaiReviewResponse {
        String id;
        Integer mondaiNo;
        Integer orderNo;
        String instruction;
        List<QuestionGroupReviewResponse> questionGroups;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionGroupReviewResponse {
        String id;
        Integer orderNo;
        String passage;
        String imageUrl;
        String transcript; // Trả ra sau submit
        List<QuestionReviewResponse> questions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionReviewResponse {
        String id;
        Integer orderNo;
        Integer questionNo;
        String content;
        String explanation; // Trả ra sau submit
        String selectedOptionId;
        Boolean correct;
        Boolean flagged;
        List<OptionReviewResponse> options;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionReviewResponse {
        String id;
        Integer optionNo;
        String optionText;
        String imageUrl;
        Boolean isCorrect; // Trả ra sau submit
    }
}
