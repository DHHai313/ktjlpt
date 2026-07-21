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
@Table(
        name = "exam_sections",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exam_section_order",
                columnNames = {"exam_id", "section_order"}
        )
)
public class ExamSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Column(name = "title", length = 100, nullable = false)
    String title;

    @Column(name = "section_type", length = 20, nullable = false)
    String sectionType;

    @Column(name = "section_order", nullable = false)
    Integer sectionOrder;

    @Column(name = "time_limit")
    Integer timeLimit;

    @Column(name = "audio_url", length = 500)
    String audioUrl;

    @OneToMany(mappedBy = "examSection", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuestionGroup> questionGroups;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
