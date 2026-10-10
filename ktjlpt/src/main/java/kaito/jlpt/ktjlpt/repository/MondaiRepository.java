package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Mondai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MondaiRepository extends JpaRepository<Mondai, String> {

    List<Mondai> findBySectionIdOrderByOrderNoAsc(String sectionId);
}
