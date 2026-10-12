package kaito.jlpt.ktjlpt.dto.response;

import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.enums.SectionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamAdminDetailResponse {
    String id;
    String title;
    JlptLevel level;
    Integer durationMinutes;
    Boolean published;
    Instant createdAt;
    Instant updatedAt;
    List<SectionAdminResponse> sections;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SectionAdminResponse {
        String id;
        SectionType sectionType;
        String title;
        Integer orderNo;
        String audioUrl;
        List<MondaiAdminResponse> mondaiList;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MondaiAdminResponse {
        String id;
        Integer mondaiNo;
        Integer orderNo;
        String instruction;
        List<QuestionGroupAdminResponse> questionGroups;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestionGroupAdminResponse {
        String id;
        Integer orderNo;
        String passage;
        String imageUrl;
        String transcript;
        List<QuestionAdminResponse> questions;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestionAdminResponse {
        String id;
        Integer orderNo;
        Integer questionNo;
        String content;
        String explanation;
        List<OptionAdminResponse> options;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OptionAdminResponse {
        String id;
        Integer optionNo;
        String optionText;
        String imageUrl;
        Boolean correct;
    }
}
