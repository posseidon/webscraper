package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuestionRepository extends MongoRepository<Question, String> {
    List<Question> findByTopicId(String topicId);

    List<Question> findByTopicIdIn(List<String> topicIds);

    List<Question> findByQuestionIn(List<String> questions);
}
