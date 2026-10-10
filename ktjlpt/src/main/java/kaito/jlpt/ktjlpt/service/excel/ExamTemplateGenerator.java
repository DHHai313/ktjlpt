package kaito.jlpt.ktjlpt.service.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ExamTemplateGenerator {

    private static final String[] EXAM_INFO_HEADERS = {"Title", "Level", "DurationMinutes"};

    private static final String[] QUESTION_HEADERS = {
            "Section", "Section Audio", "Mondai No", "Mondai Instruction",
            "Group Id", "Passage", "Image Url", "Transcript",
            "Question No", "Question Content", "Explanation",
            "Option 1", "Option 2", "Option 3", "Option 4", "Correct Option"
    };

    public byte[] generateTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ─── Style cho header ───
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ─── Sheet 1: Exam_Info ───
            Sheet examSheet = workbook.createSheet("Exam_Info");
            Row examHeader = examSheet.createRow(0);
            for (int i = 0; i < EXAM_INFO_HEADERS.length; i++) {
                Cell cell = examHeader.createCell(i);
                cell.setCellValue(EXAM_INFO_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                examSheet.setColumnWidth(i, 6000);
            }
            // Dòng dữ liệu mẫu
            Row examSample = examSheet.createRow(1);
            examSample.createCell(0).setCellValue("JLPT N3 - 2024年7月");
            examSample.createCell(1).setCellValue("N3");
            examSample.createCell(2).setCellValue(140);

            // ─── Sheet 2: Questions ───
            Sheet qSheet = workbook.createSheet("Questions");
            Row qHeader = qSheet.createRow(0);
            for (int i = 0; i < QUESTION_HEADERS.length; i++) {
                Cell cell = qHeader.createCell(i);
                cell.setCellValue(QUESTION_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                qSheet.setColumnWidth(i, 5000);
            }
            // Wider columns for text content
            qSheet.setColumnWidth(3, 10000);  // Mondai Instruction
            qSheet.setColumnWidth(5, 12000);  // Passage
            qSheet.setColumnWidth(9, 10000);  // Question Content
            qSheet.setColumnWidth(10, 10000); // Explanation

            // ─── Dữ liệu mẫu: VOCAB section ───
            int r = 1;
            // Câu 1 - VOCAB, mondai 1, group đơn lẻ
            Row row1 = qSheet.createRow(r++);
            setCells(row1, "VOCAB", "", "1", "つぎの ぶんの ＿＿の ことばの よみかたとして もっとも よい ものを、1・2・3・4 から ひとつ えらびなさい。",
                    "", "", "", "",
                    "1", "この 本は 内容 が むずかしい。", "内容 = ないよう (nội dung)",
                    "ないよう", "うちよう", "ないみ", "うちみ", "1");

            // Câu 2 - cùng mondai 1
            Row row2 = qSheet.createRow(r++);
            setCells(row2, "VOCAB", "", "1", "",
                    "", "", "", "",
                    "2", "あの 店は 品物 が やすい。", "品物 = しなもの (hàng hóa)",
                    "しなもの", "ひんもつ", "しなぶつ", "ひんぶつ", "1");

            // ─── Dữ liệu mẫu: READING section, passage group ───
            Row row3 = qSheet.createRow(r++);
            setCells(row3, "READING", "", "1", "つぎの ぶんしょうを よんで、しつもんに こたえなさい。",
                    "G1", "日本では、春になると桜の花が咲きます。多くの人が公園に集まり、花見を楽しみます。...", "", "",
                    "30", "この 文章で「花見」は なんですか。", "花見 = お花見、桜を見ながら楽しむこと",
                    "桜の花を見て楽しむこと", "花を買うこと", "花を植えること", "花を描くこと", "1");

            Row row4 = qSheet.createRow(r++);
            setCells(row4, "READING", "", "1", "",
                    "G1", "", "", "",
                    "31", "日本人は 春に よく なにを しますか。", "",
                    "花見に行く", "海に行く", "山に登る", "旅行に行く", "1");

            // ─── Dữ liệu mẫu: LISTENING section ───
            Row row5 = qSheet.createRow(r++);
            setCells(row5, "LISTENING", "https://example.com/audio/n3_2024_listening.mp3", "1", "もんだい1では、まず しつもんを 聞いて ください。",
                    "", "", "", "男：すみません、この電車は東京駅に止まりますか。\n女：はい、次の駅です。",
                    "50", "", "電車は次の駅で東京駅に止まる",
                    "東京駅", "新宿駅", "渋谷駅", "品川駅", "1");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setCells(Row row, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && !values[i].isEmpty()) {
                row.createCell(i).setCellValue(values[i]);
            }
        }
    }
}
