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
    name = "question_groups",
    indexes = {
        @Index(name = "idx_qg_mondai_id", columnList = "mondai_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"mondai_id", "order_no"})
    }
)
public class QuestionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mondai_id", nullable = false)
    Mondai mondai;

    @Column(name = "order_no", nullable = false)
    Integer orderNo;

    // Bài đọc / đoạn văn đục lỗ ([1], [2]...). Null nếu không có
    @Column(name = "passage", columnDefinition = "TEXT")
    String passage;

    // Ảnh hoặc bảng dùng chung cho cả nhóm
    @Column(name = "image_url", length = 500)
    String imageUrl;

    // Lời thoại phần nghe, chỉ trả sau khi nộp bài
    @Column(name = "transcript", columnDefinition = "TEXT")
    String transcript;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Question> questions;
}
