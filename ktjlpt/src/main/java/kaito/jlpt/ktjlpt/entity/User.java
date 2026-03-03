package kaito.jlpt.ktjlpt.entity;

import jakarta.persistence.*;
import kaito.jlpt.ktjlpt.enums.Provider;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Set;

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

    @Column(nullable = false, unique = true)
    String username;

    @Column(nullable = false)
    String password;

    @ManyToMany(fetch = FetchType.EAGER)
    Set<Role> roles;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "current_level", length = 5)
    String currentLevel;

    @Column(name = "avatar_url", length = 512)
    String avatarUrl;

    @Column(name = "is_active")
    @Builder.Default
    Boolean active=true;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    Provider  provider;

    @CreationTimestamp
    @Column(name = "last_login_at")
    Instant lastLoginAt;

    @CreationTimestamp // Tự động điền thời gian khi tạo bản ghi
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp // Tự động cập nhật thời gian khi sửa bản ghi
    @Column(name = "updated_at")
    Instant updatedAt;
}
