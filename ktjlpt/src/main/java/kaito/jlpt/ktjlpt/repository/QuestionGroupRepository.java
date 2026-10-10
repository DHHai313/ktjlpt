package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.QuestionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup, String> {

    List<QuestionGroup> findByMondaiIdOrderByOrderNoAsc(String mondaiId);
}
