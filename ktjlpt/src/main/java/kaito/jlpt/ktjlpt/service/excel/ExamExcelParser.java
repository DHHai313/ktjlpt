package kaito.jlpt.ktjlpt.service.excel;

import kaito.jlpt.ktjlpt.dto.excel.ExcelImportErrorDto;
import kaito.jlpt.ktjlpt.entity.*;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import kaito.jlpt.ktjlpt.enums.SectionType;
import kaito.jlpt.ktjlpt.exception.AppException;
import kaito.jlpt.ktjlpt.enums.ErrorCode;
import kaito.jlpt.ktjlpt.exception.ExcelValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamExcelParser {

    public Exam parseExcel(Workbook workbook) {
        List<ExcelImportErrorDto> errors = new ArrayList<>();
        Exam exam = new Exam();
        exam.setSections(new ArrayList<>());
        exam.setPublished(false);

        parseExamInfo(workbook, exam, errors);
        if (!errors.isEmpty()) {
            throw new ExcelValidationException(errors);
        }

        parseQuestions(workbook, exam, errors);
        if (!errors.isEmpty()) {
            throw new ExcelValidationException(errors);
        }

        return exam;
    }

    private void parseExamInfo(Workbook workbook, Exam exam, List<ExcelImportErrorDto> errors) {
        Sheet sheet = workbook.getSheet("Exam_Info");
        if (sheet == null) {
            throw new AppException(ErrorCode.EXCEL_MISSING_SHEET);
        }

        Row row = sheet.getRow(1);
        if (row == null) {
            errors.add(ExcelImportErrorDto.builder().row(1).column("All").message("Exam_Info data row is missing").build());
            return;
        }

        String title = ExcelHelper.readString(row, 0);
        String levelStr = ExcelHelper.readString(row, 1);
        Integer duration = ExcelHelper.readInteger(row, 2);

        if (title == null || title.isBlank()) {
            errors.add(ExcelImportErrorDto.builder().row(1).column("Title").message("Title is required").build());
        } else {
            exam.setTitle(title);
        }

        if (levelStr == null || levelStr.isBlank()) {
            errors.add(ExcelImportErrorDto.builder().row(1).column("Level").message("Level is required").build());
        } else {
            try {
                exam.setLevel(JlptLevel.valueOf(levelStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                errors.add(ExcelImportErrorDto.builder().row(1).column("Level").message("Invalid Level: " + levelStr).build());
            }
        }

        if (duration == null) {
            errors.add(ExcelImportErrorDto.builder().row(1).column("DurationMinutes").message("DurationMinutes is required").build());
        } else {
            exam.setDurationMinutes(duration);
        }
    }

    private void parseQuestions(Workbook workbook, Exam exam, List<ExcelImportErrorDto> errors) {
        Sheet sheet = workbook.getSheet("Questions");
        if (sheet == null) {
            throw new AppException(ErrorCode.EXCEL_MISSING_SHEET);
        }

        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 1) {
            errors.add(ExcelImportErrorDto.builder().row(0).column("All").message("Questions sheet has no data").build());
            return;
        }

        Section currentSection = null;
        Mondai currentMondai = null;
        QuestionGroup currentGroup = null;
        String currentGroupIdStr = null;

        int sectionOrder = 1;
        int mondaiOrder = 1;
        int groupOrder = 1;
        int questionOrderInGroup = 1;

        Set<Integer> questionNumbers = new HashSet<>();

        for (int i = 1; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String sectionStr = ExcelHelper.readString(row, 0);
            String sectionAudio = ExcelHelper.readString(row, 1);
            Integer mondaiNo = ExcelHelper.readInteger(row, 2);
            String mondaiInstruction = ExcelHelper.readString(row, 3);
            String groupId = ExcelHelper.readString(row, 4);
            String passage = ExcelHelper.readString(row, 5);
            String imageUrl = ExcelHelper.readString(row, 6);
            String transcript = ExcelHelper.readString(row, 7);
            Integer questionNo = ExcelHelper.readInteger(row, 8);
            String questionContent = ExcelHelper.readString(row, 9);
            String explanation = ExcelHelper.readString(row, 10);
            String opt1 = ExcelHelper.readString(row, 11);
            String opt2 = ExcelHelper.readString(row, 12);
            String opt3 = ExcelHelper.readString(row, 13);
            String opt4 = ExcelHelper.readString(row, 14);
            Integer correctOption = ExcelHelper.readInteger(row, 15);

            boolean rowHasErrors = false;

            if (sectionStr == null || sectionStr.isBlank()) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Section").message("Section is required").build());
                rowHasErrors = true;
            }
            if (mondaiNo == null) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Mondai No").message("Mondai No is required").build());
                rowHasErrors = true;
            }
            if (questionNo == null) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Question No").message("Question No is required").build());
                rowHasErrors = true;
            }
            if (opt1 == null || opt1.isBlank()) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Option 1").message("Option 1 is required").build());
                rowHasErrors = true;
            }
            if (opt2 == null || opt2.isBlank()) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Option 2").message("Option 2 is required").build());
                rowHasErrors = true;
            }
            if (opt3 == null || opt3.isBlank()) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Option 3").message("Option 3 is required").build());
                rowHasErrors = true;
            }
            if (opt4 == null || opt4.isBlank()) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Option 4").message("Option 4 is required").build());
                rowHasErrors = true;
            }
            if (correctOption == null || correctOption < 1 || correctOption > 4) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Correct Option").message("Correct Option must be between 1 and 4").build());
                rowHasErrors = true;
            }
            if (questionNo != null && !questionNumbers.add(questionNo)) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Question No").message("Question No must be unique: " + questionNo).build());
                rowHasErrors = true;
            }

            if (rowHasErrors) continue;

            SectionType sectionType = null;
            try {
                sectionType = SectionType.valueOf(sectionStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(ExcelImportErrorDto.builder().row(i).column("Section").message("Invalid Section: " + sectionStr).build());
                continue;
            }

            if (currentSection == null || currentSection.getSectionType() != sectionType) {
                currentSection = new Section();
                currentSection.setExam(exam);
                currentSection.setSectionType(sectionType);
                currentSection.setTitle(getSectionTitle(sectionType));
                currentSection.setOrderNo(sectionOrder++);
                currentSection.setAudioUrl(sectionAudio);
                currentSection.setMondaiList(new ArrayList<>());
                exam.getSections().add(currentSection);
                
                currentMondai = null;
                mondaiOrder = 1;
            }

            if (currentMondai == null || !currentMondai.getMondaiNo().equals(mondaiNo)) {
                currentMondai = new Mondai();
                currentMondai.setSection(currentSection);
                currentMondai.setMondaiNo(mondaiNo);
                currentMondai.setOrderNo(mondaiOrder++);
                currentMondai.setInstruction(mondaiInstruction);
                currentMondai.setQuestionGroups(new ArrayList<>());
                currentSection.getMondaiList().add(currentMondai);

                currentGroup = null;
                groupOrder = 1;
                currentGroupIdStr = null;
            }

            boolean isNewGroup = false;
            if (groupId == null || groupId.isBlank()) {
                isNewGroup = true;
                currentGroupIdStr = null;
            } else if (!groupId.equals(currentGroupIdStr)) {
                isNewGroup = true;
                currentGroupIdStr = groupId;
            }

            if (isNewGroup || currentGroup == null) {
                currentGroup = new QuestionGroup();
                currentGroup.setMondai(currentMondai);
                currentGroup.setOrderNo(groupOrder++);
                currentGroup.setPassage(passage);
                currentGroup.setImageUrl(imageUrl);
                currentGroup.setTranscript(transcript);
                currentGroup.setQuestions(new ArrayList<>());
                currentMondai.getQuestionGroups().add(currentGroup);
                questionOrderInGroup = 1;
            }

            Question question = new Question();
            question.setGroup(currentGroup);
            question.setExam(exam);
            question.setOrderNo(questionOrderInGroup++);
            question.setQuestionNo(questionNo);
            question.setContent(questionContent);
            question.setExplanation(explanation);
            question.setOptions(new ArrayList<>());
            currentGroup.getQuestions().add(question);

            addOption(question, 1, opt1, correctOption == 1);
            addOption(question, 2, opt2, correctOption == 2);
            addOption(question, 3, opt3, correctOption == 3);
            addOption(question, 4, opt4, correctOption == 4);
        }
    }

    private void addOption(Question question, int optionNo, String text, boolean isCorrect) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setOptionNo(optionNo);
        option.setOptionText(text);
        option.setCorrect(isCorrect);
        question.getOptions().add(option);
    }

    private String getSectionTitle(SectionType type) {
        if (type == null) return "";
        return switch (type) {
            case VOCAB -> "文字・語彙";
            case GRAMMAR -> "文法";
            case READING -> "読解";
            case LISTENING -> "聴解";
        };
    }
}
