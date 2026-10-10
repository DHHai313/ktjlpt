package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.Section;
import kaito.jlpt.ktjlpt.enums.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, String> {

    List<Section> findByExamIdOrderByOrderNoAsc(String examId);

    Optional<Section> findByExamIdAndSectionType(String examId, SectionType sectionType);
}
