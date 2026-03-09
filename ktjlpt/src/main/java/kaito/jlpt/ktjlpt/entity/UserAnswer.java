package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_answers")
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    UserExamAttempts attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    Question question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

}
