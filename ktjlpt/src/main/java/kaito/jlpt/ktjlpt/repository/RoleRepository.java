package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
