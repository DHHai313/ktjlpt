package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "question_options",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_question_option_order",
                columnNames = {"question_id", "option_order"}
        )
)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "option_order", nullable = false)
    Integer optionOrder;

    @Column(name = "is_correct")
    @Builder.Default
    Boolean isCorrect = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
