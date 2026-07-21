package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
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
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "level", length = 10, nullable = false)
    String level;

    @Column(name = "total_time", nullable = false)
    Integer totalTime;

    @Column(name = "total_score")
    @Builder.Default
    Integer totalScore = 180;

    @Column(name = "pass_score")
    @Builder.Default
    Integer passScore = 90;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExamSection> sections;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
