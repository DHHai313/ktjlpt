package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, String> {

    /**
     * Lấy tất cả options của một câu hỏi, sắp theo thứ tự option.
     */
    List<QuestionOption> findByQuestionIdOrderByOptionOrder(String questionId);
}
