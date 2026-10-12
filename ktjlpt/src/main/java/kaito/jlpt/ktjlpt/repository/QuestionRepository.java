package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, String> {

    List<Question> findByExamIdOrderByQuestionNoAsc(String examId);

    List<Question> findByGroupIdOrderByOrderNoAsc(String groupId);

    // Đếm số câu hỏi trong một đề thi
    long countByExamId(String examId);

    // Đếm số câu theo section (qua mondai → section)
    @Query("SELECT COUNT(q) FROM Question q " +
           "JOIN q.group g JOIN g.mondai m " +
           "WHERE m.section.id = :sectionId")
    long countByExamIdAndSectionId(String sectionId);

    @Query("SELECT q FROM Question q WHERE q.group.id IN :groupIds ORDER BY q.orderNo ASC")
    List<Question> findByGroupIdInOrderByOrderNoAsc(@Param("groupIds") List<String> groupIds);
}
