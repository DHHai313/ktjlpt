package kaito.jlpt.ktjlpt.repository;

import kaito.jlpt.ktjlpt.entity.AttemptSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttemptSectionRepository extends JpaRepository<AttemptSection, String> {

    List<AttemptSection> findByAttemptId(String attemptId);

    Optional<AttemptSection> findByAttemptIdAndSectionId(String attemptId, String sectionId);
}
