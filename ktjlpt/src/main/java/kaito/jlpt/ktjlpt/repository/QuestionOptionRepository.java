package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, String> {

    List<QuestionOption> findByQuestionIdOrderByOptionNoAsc(String questionId);

    @Query("SELECT o FROM QuestionOption o WHERE o.question.id IN :questionIds ORDER BY o.optionNo ASC")
    List<QuestionOption> findByQuestionIdInOrderByOptionNoAsc(@Param("questionIds") List<String> questionIds);
}
