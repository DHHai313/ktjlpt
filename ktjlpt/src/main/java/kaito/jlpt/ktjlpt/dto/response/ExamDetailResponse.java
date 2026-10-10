package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.SectionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * DTO trả về khi user bắt đầu làm bài.
 * Không chứa: is_correct của option, transcript của group (chỉ trả sau submit).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamDetailResponse {
    String id;
    String title;
    String level;
    Integer durationMinutes;
    List<SectionResponse> sections;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionResponse {
        String id;
        SectionType sectionType;
        String title;
        Integer orderNo;
        String audioUrl;
        List<MondaiResponse> mondaiList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MondaiResponse {
        String id;
        Integer mondaiNo;
        Integer orderNo;
        String instruction;
        List<QuestionGroupResponse> questionGroups;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionGroupResponse {
        String id;
        Integer orderNo;
        String passage;
        String imageUrl;
        // transcript KHÔNG có ở đây (chỉ trả sau submit)
        List<QuestionResponse> questions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionResponse {
        String id;
        Integer orderNo;
        Integer questionNo;
        String content;
        // explanation KHÔNG có ở đây (chỉ trả sau submit)
        List<OptionResponse> options;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionResponse {
        String id;
        Integer optionNo;
        String optionText;
        String imageUrl;
        // isCorrect KHÔNG có ở đây (chỉ trả sau submit)
    }
}
