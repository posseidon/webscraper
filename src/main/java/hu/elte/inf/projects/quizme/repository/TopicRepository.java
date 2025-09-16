package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Topic;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends MongoRepository<Topic, String> {

    Optional<Topic> findByTopicId(String topicId);

    List<Topic> findByTopicIdIn(List<String> topicIds);

    List<Topic> findByTitleName(String titleName);

    List<Topic> findByTopicName(String topicName);
}
