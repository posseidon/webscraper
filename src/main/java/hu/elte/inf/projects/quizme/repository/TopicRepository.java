package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, String> {

    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.questions WHERE t.topicId = :topicId")
    Optional<Topic> findByTopicId(@Param("topicId") String topicId);
}
