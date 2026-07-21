package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    /**
     * Lấy tất cả câu hỏi trong một group, sắp theo thứ tự câu hỏi.
     */
    List<Question> findByQuestionGroupIdOrderByQuestionOrder(String groupId);
}
