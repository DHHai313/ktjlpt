package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(nullable = false, length = 255)
    String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    JlptLevel level;

    @Column(name = "duration_minutes", nullable = false)
    Integer durationMinutes;

    @Column(name = "is_published", nullable = false)
    @Builder.Default
    Boolean published = false;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Section> sections;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
