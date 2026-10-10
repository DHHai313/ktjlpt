package kaito.jlpt.ktjlpt.dto.excel;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO map from each row of the Questions sheet in the Excel file.
 * Each row corresponds to 1 Question with Section/Mondai/Group context.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamImportRowDto {
    int rowNum;
    String section;
    String sectionAudio;
    Integer mondaiNo;
    String mondaiInstruction;
    String groupId;
    String passage;
    String imageUrl;
    String transcript;
    Integer questionNo;
    String questionContent;
    String explanation;
    String option1;
    String option2;
    String option3;
    String option4;
    Integer correctOption;
}
