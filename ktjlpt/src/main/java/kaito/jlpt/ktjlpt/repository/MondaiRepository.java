package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Mondai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MondaiRepository extends JpaRepository<Mondai, String> {

    List<Mondai> findBySectionIdOrderByOrderNoAsc(String sectionId);

    @Query("SELECT m FROM Mondai m WHERE m.section.id IN :sectionIds ORDER BY m.orderNo ASC")
    List<Mondai> findBySectionIdInOrderByOrderNoAsc(@Param("sectionIds") List<String> sectionIds);
}
