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
    name = "attempt_sections",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"attempt_id", "section_id"})
    }
)
public class AttemptSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    ExamAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    Section section;

    @Column(name = "started_at")
    Instant startedAt;

    // Deadline của section
    @Column(name = "expires_at")
    Instant expiresAt;

    @Column(name = "submitted_at")
    Instant submittedAt;
}
