package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, String> {

    /**
     * Lấy tất cả answers của một attempt.
     */
    List<UserAnswer> findByAttemptId(String attemptId);

    /**
     * Kiểm tra user đã trả lời câu hỏi này trong attempt chưa.
     */
    boolean existsByAttemptIdAndQuestionId(String attemptId, String questionId);

    /**
     * Lấy answer của một câu hỏi cụ thể trong attempt (dùng khi UPDATE answer).
     */
    Optional<UserAnswer> findByAttemptIdAndQuestionId(String attemptId, String questionId);
}
