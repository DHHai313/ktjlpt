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
    @OneToMany(mappedBy = "exams", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<ExamSection> sections;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

}
