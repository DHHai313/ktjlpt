package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import kaito.jlpt.ktjlpt.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(name = "user_name", length = 100)
    String userName;


    /**
     * Role đơn giản: "USER" hoặc "ADMIN".
     */
    @Column(name = "role", length = 20)
    Role role;

    @Column(name = "avatar_url", length = 512)
    String avatarUrl;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;


    @Column(name = "last_login_at")
    Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;
}
