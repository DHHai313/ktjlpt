package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import kaito.jlpt.ktjlpt.enums.SectionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
    name = "sections",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "section_type"}),
        @UniqueConstraint(columnNames = {"exam_id", "order_no"})
    }
)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 20)
    SectionType sectionType;

    @Column(name = "title", length = 100)
    String title;

    @Column(name = "order_no", nullable = false)
    Integer orderNo;

    // File nghe full, chỉ dùng cho LISTENING
    @Column(name = "audio_url", length = 500)
    String audioUrl;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Mondai> mondaiList;
}
