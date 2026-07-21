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
        name = "user_exam_attempts",
        indexes = {
                @Index(name = "idx_attempt_user_id", columnList = "user_id"),
                @Index(name = "idx_attempt_user_exam", columnList = "user_id, exam_id")
        }
)
public class UserExamAttempts {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Column(name = "total_score")
    Integer totalScore;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    Instant startedAt;

    @Column(name = "completed_at")
    Instant completedAt;

    @Column(name = "status", length = 20)
    @Builder.Default
    String status = "IN_PROGRESS";
}
