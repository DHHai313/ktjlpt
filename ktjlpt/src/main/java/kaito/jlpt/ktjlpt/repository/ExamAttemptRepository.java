package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.ExamAttempt;
import kaito.jlpt.ktjlpt.enums.AttemptMode;
import kaito.jlpt.ktjlpt.enums.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, String> {

    List<ExamAttempt> findByUserIdOrderByStartedAtDesc(String userId);

    List<ExamAttempt> findByUserIdAndExamId(String userId, String examId);

    List<ExamAttempt> findByUserIdAndMode(String userId, AttemptMode mode);

    List<ExamAttempt> findByUserIdAndModeAndStatus(String userId, AttemptMode mode, AttemptStatus status);

    // Tìm lượt thi đang còn hạn (để check duplicate in-progress)
    Optional<ExamAttempt> findByUserIdAndExamIdAndStatus(String userId, String examId, AttemptStatus status);

    // Tìm các attempt đã hết hạn nhưng chưa EXPIRED (dùng cho scheduler)
    @Query("SELECT a FROM ExamAttempt a WHERE a.status = 'IN_PROGRESS' AND a.expiresAt IS NOT NULL AND a.expiresAt < :now")
    List<ExamAttempt> findExpiredAttempts(Instant now);
}
