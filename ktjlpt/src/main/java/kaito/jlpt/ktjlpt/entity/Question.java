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
        name = "questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_question_order",
                columnNames = {"group_id", "question_order"}
        )
)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    QuestionGroup questionGroup;

    /**
     * Passage riêng cho từng câu hỏi (nullable) — bổ sung so với passage của nhóm.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passage_id")
    Passage passage;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "image_url", length = 500)
    String imageUrl;

    @Column(name = "question_order", nullable = false)
    Integer questionOrder;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuestionOption> options;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
}
