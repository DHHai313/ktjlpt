package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_exam_attempts")
public class UserExamAttempts {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Column(name = "total_score")
    Integer totalScore;

    @CreationTimestamp
    @Column(name = "started_at")
    Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "status")
    String status;
}
