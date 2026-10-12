package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.dto.response.ExamAdminSummaryResponse;
import kaito.jlpt.ktjlpt.entity.Exam;
import kaito.jlpt.ktjlpt.enums.JlptLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    List<Exam> findByPublishedTrue();

    List<Exam> findByLevelAndPublishedTrue(JlptLevel level);

    List<Exam> findByLevel(JlptLevel level);

    List<Exam> findByPublished(Boolean published);

    List<Exam> findByLevelAndPublished(JlptLevel level, Boolean published);

    @Query(value = """
            SELECT new kaito.jlpt.ktjlpt.dto.response.ExamAdminSummaryResponse(
                e.id,
                e.title,
                e.level,
                e.durationMinutes,
                e.published,
                e.createdAt,
                e.updatedAt,
                (SELECT COUNT(s) FROM Section s WHERE s.exam.id = e.id),
                (SELECT COUNT(q) FROM Question q WHERE q.exam.id = e.id)
            )
            FROM Exam e
            WHERE (:level IS NULL OR e.level = :level)
              AND (:published IS NULL OR e.published = :published)
              AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(e)
            FROM Exam e
            WHERE (:level IS NULL OR e.level = :level)
              AND (:published IS NULL OR e.published = :published)
              AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ExamAdminSummaryResponse> findAllAdmin(
            @Param("level") JlptLevel level,
            @Param("published") Boolean published,
            @Param("keyword") String keyword,
            Pageable pageable);
}
