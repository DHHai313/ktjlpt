package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
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
    name = "mondai",
    indexes = {
        @Index(name = "idx_mondai_section_id", columnList = "section_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"section_id", "order_no"})
    }
)
public class Mondai {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    Section section;

    // 問題1, 問題2...
    @Column(name = "mondai_no", nullable = false)
    Integer mondaiNo;

    @Column(name = "order_no", nullable = false)
    Integer orderNo;

    // Đề bài chung của mondai
    @Column(name = "instruction", columnDefinition = "TEXT")
    String instruction;

    @OneToMany(mappedBy = "mondai", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<QuestionGroup> questionGroups;
}
