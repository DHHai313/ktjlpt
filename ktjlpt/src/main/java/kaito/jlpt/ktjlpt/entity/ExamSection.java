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
@Table(name = "exam_sections")
public class ExamSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exams;
    @Column(name = "section_type", nullable = false)
    String sectionType;
    @Column(name = "section_order", nullable = false)
    Integer sectionOrder;
    @Column(name = "time_limit")
    Integer timeLimit;
    @Column(name = "audio_url")
    String audioUrl;
    @OneToMany(mappedBy = "examSection", fetch = FetchType.LAZY)
    List<QuestionGroup> questionGroups;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;


}
