package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    /**
     * Lấy danh sách Exam theo level, sắp xếp mới nhất trước.
     */
    List<Exam> findByLevelOrderByCreatedAtDesc(String level);

    /**
     * Kiểm tra tồn tại theo title + level để tránh tạo trùng.
     */
    boolean existsByTitleAndLevel(String title, String level);
}
