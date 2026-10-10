package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import kaito.jlpt.ktjlpt.enums.AttemptMode;
import kaito.jlpt.ktjlpt.enums.AttemptScope;
import kaito.jlpt.ktjlpt.enums.AttemptStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "exam_attempts",
    indexes = {
        @Index(name = "idx_attempt_user_exam_mode", columnList = "user_id, exam_id, mode"),
        @Index(name = "idx_attempt_user_mode_status", columnList = "user_id, mode, status"),
        @Index(name = "idx_attempt_status_expires", columnList = "status, expires_at")
    }
)
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AttemptMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    AttemptScope scope = AttemptScope.FULL;

    // Có giá trị khi scope = SECTION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    Section section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    AttemptStatus status = AttemptStatus.IN_PROGRESS;

    // Thời lượng áp dụng cho lượt thi; NULL nếu không giới hạn (PRACTICE)
    @Column(name = "duration_minutes")
    Integer durationMinutes;

    @Column(name = "started_at", nullable = false)
    Instant startedAt;

    // Deadline do server xác định; NULL nếu không giới hạn
    @Column(name = "expires_at")
    Instant expiresAt;

    @Column(name = "submitted_at")
    Instant submittedAt;

    // Tổng thời gian làm bài thực tế (giây)
    @Column(name = "time_spent_seconds")
    Integer timeSpentSeconds;

    @Column(name = "correct_count")
    Integer correctCount;

    // Số câu trong phạm vi lượt làm bài
    @Column(name = "total_questions")
    Integer totalQuestions;

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<AttemptAnswer> answers;

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<AttemptSection> attemptSections;
}
