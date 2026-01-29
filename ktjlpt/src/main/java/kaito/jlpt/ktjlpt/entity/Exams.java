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
@Table(name = "exams")
public class Exams {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)

    String id;

    @Column(name = "title")
    String title;

    @Column(name = "level")
    String level;

    @Column(name = "total_time")
    Integer totalTime;

    @Column(name = "total_score")
    Integer totalScore;

    @Column(name = "pass_score")
    Integer passScore;

    @Column(name = "is_active")
    @Builder.Default
    boolean isActive=true;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
