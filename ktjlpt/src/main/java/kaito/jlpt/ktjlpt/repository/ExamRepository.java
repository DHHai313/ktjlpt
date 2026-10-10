package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Exam;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    List<Exam> findByPublishedTrue();

    List<Exam> findByLevelAndPublishedTrue(JlptLevel level);

    List<Exam> findByLevel(JlptLevel level);
}
