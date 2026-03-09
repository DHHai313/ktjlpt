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
@Table(name = "question_groups")
public class QuestionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    ExamSection examSections;

    @ManyToOne
    @JoinColumn(name = "passage_id")
    private Passage passage;

    @Column(name = "content")
    String content;

    @Column(name = "image_url")
    String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;

}
