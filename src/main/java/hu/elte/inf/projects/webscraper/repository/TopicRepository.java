package hu.elte.inf.projects.webscraper.repository;

import hu.elte.inf.projects.webscraper.repository.dto.Topic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, String> {
    
    Optional<Topic> findByTopicName(String topicName);
    
    @EntityGraph(value = "Topic.withQuestions")
    Optional<Topic> findWithQuestionsByTopicName(String topicName);
}
