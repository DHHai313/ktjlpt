package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "questions",
    indexes = {
        @Index(name = "idx_question_group_id", columnList = "group_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "question_no"})
    }
)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    QuestionGroup group;

    // Denormalize để load cả đề thuận tiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    // Thứ tự trong group
    @Column(name = "order_no", nullable = false)
    Integer orderNo;

    // Thứ tự toàn đề, dùng cho bảng điều hướng
    @Column(name = "question_no", nullable = false)
    Integer questionNo;

    // Null nếu câu hỏi chỉ có nhãn A/B/C
    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    // Giải thích đáp án sau khi nộp bài
    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<QuestionOption> options;
}
