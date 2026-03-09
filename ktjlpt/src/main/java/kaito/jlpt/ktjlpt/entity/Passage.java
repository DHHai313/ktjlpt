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
@Table(name = "passages")
public class Passage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "content")
    String content;

    @Column(name = "image_url")
    String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    Instant createdAt;
}
