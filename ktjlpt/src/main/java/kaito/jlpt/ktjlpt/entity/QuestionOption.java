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
@Table(name = "question_groups")
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    @Column(nullable = false)
    String content;

    @Column(name = "option_order")
    Integer optionOrder;

    @Column(name = "is_correct")
    Boolean isCorrect = false;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
