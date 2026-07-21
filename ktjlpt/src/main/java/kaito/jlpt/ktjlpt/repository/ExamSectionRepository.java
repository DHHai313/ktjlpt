package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.ExamSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamSectionRepository extends JpaRepository<ExamSection, String> {

    /**
     * Lấy tất cả sections của một exam, sắp theo thứ tự section.
     */
    List<ExamSection> findByExamIdOrderBySectionOrder(String examId);

    /**
     * Kiểm tra xem section_order đã tồn tại trong exam chưa.
     */
    boolean existsByExamIdAndSectionOrder(String examId, Integer sectionOrder);
}
