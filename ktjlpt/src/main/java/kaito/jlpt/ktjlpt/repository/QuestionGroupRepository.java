package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.QuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, String> {

    List<QuestionGroup> findByMondaiIdOrderByOrderNoAsc(String mondaiId);

    @Query("SELECT g FROM QuestionGroup g WHERE g.mondai.id IN :mondaiIds ORDER BY g.orderNo ASC")
    List<QuestionGroup> findByMondaiIdInOrderByOrderNoAsc(@Param("mondaiIds") List<String> mondaiIds);
}
