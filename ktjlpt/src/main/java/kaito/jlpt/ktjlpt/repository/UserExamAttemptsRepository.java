package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.UserExamAttempts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExamAttemptsRepository extends JpaRepository<UserExamAttempts, String> {

    /**
     * Lịch sử thi của một user, mới nhất trước.
     */
    List<UserExamAttempts> findByUserIdOrderByStartedAtDesc(String userId);

    /**
     * Tất cả attempts của user cho một exam cụ thể.
     */
    List<UserExamAttempts> findByUserIdAndExamId(String userId, String examId);

    /**
     * Lấy attempt theo ID + userId — để đảm bảo user chỉ xem attempt của chính mình.
     */
    Optional<UserExamAttempts> findByIdAndUserId(String id, String userId);
}
