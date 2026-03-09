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
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    QuestionGroup group;

    @ManyToOne
    @JoinColumn(name = "passage_id")
    Passage passage;

    @Column(nullable = false)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "question_order")
    private Integer questionOrder;

    @OneToMany(mappedBy = "questions", fetch = FetchType.LAZY)
    private List<QuestionOption> options;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

}
