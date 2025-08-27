package hu.elte.inf.projects.webscraper.service.json;

import hu.elte.inf.projects.webscraper.repository.QuestionRepository;
import hu.elte.inf.projects.webscraper.repository.QuizMetaDataRepository;
import hu.elte.inf.projects.webscraper.repository.TopicRepository;
import hu.elte.inf.projects.webscraper.repository.dto.QuizMetadata;
import hu.elte.inf.projects.webscraper.repository.dto.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QuizService {

    private final QuizMetaDataRepository metaDataRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuizService(QuizMetaDataRepository metaDataRepository, TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.metaDataRepository = metaDataRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    public Topic findTopicByName(String topicName) {
        Optional<Topic> topicOptional = topicRepository.findByTopicName(topicName);
        return topicOptional.orElse(null);
    }

    public QuizMetadata findCompleteQuizByTitle(String title) {
        Optional<QuizMetadata> quizOptional = metaDataRepository.findByTitle(title);
        return quizOptional.orElse(null);
    }

    public QuizMetadata findCompleteQuizById(String id) {
        Optional<QuizMetadata> quizOptional = metaDataRepository.findById(id);
        return quizOptional.orElse(null);
    }

}
