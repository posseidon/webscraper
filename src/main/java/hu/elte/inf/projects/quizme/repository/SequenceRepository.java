package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Sequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SequenceRepository extends JpaRepository<Sequence, String> {
    
    @Transactional
    @Modifying
    @Query("UPDATE Sequence s SET s.value = s.value + 1 WHERE s.name = :name")
    int incrementSequence(@Param("name") String name);
}