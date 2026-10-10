package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "attempt_answers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
    }
)
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    // NULL nếu user chưa chọn đáp án
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    QuestionOption selectedOption;

    // NULL trước khi chấm; lưu kết quả sau khi chấm
    @Column(name = "is_correct")
    Boolean correct;

    // User đánh dấu câu cần xem lại
    @Column(name = "flagged", nullable = false)
    @Builder.Default
    Boolean flagged = false;

    @Column(name = "answered_at")
    Instant answeredAt;
}
