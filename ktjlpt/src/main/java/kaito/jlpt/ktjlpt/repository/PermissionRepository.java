package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findByNameIn(Set<String> names);
}
