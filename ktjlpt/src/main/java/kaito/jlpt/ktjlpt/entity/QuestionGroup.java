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
        name = "question_groups",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_section_group_order",
                columnNames = {"section_id", "group_order"}
        )
)
public class QuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    ExamSection examSection;

    /**
     * Passage dùng chung cho cả nhóm câu hỏi (nullable).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passage_id")
    Passage passage;

    @Column(name = "title", length = 255)
    String title;

    @Column(name = "group_order", nullable = false)
    Integer groupOrder;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @OneToMany(mappedBy = "questionGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Question> questions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
