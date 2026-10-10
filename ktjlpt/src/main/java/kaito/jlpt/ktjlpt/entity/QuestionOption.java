package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "question_options",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"question_id", "option_no"})
    }
)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    // Thứ tự hiển thị: 1..4
    @Column(name = "option_no", nullable = false)
    Integer optionNo;

    // Null nếu đáp án là ảnh hoặc chỉ có nhãn A/B/C
    @Column(name = "option_text", columnDefinition = "TEXT")
    String optionText;

    // Null nếu đáp án là chữ
    @Column(name = "image_url", length = 500)
    String imageUrl;

    // Đánh dấu đáp án đúng; không trả trường này khi user đang thi
    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    Boolean correct = false;
}
