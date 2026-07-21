package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Passage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassageRepository extends JpaRepository<Passage, String> {
}
