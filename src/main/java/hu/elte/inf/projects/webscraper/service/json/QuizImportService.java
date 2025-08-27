package hu.elte.inf.projects.webscraper.service.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.inf.projects.webscraper.repository.QuestionRepository;
import hu.elte.inf.projects.webscraper.repository.QuizMetaDataRepository;
import hu.elte.inf.projects.webscraper.repository.TopicRepository;
import hu.elte.inf.projects.webscraper.repository.dto.Question;
import hu.elte.inf.projects.webscraper.repository.dto.QuizData;
import hu.elte.inf.projects.webscraper.repository.dto.QuizMetadata;
import hu.elte.inf.projects.webscraper.repository.dto.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sqlite.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizImportService {
    private static final Logger LOG = LoggerFactory.getLogger(QuizImportService.class);

    private final ObjectMapper objectMapper;
    private final QuizMetaDataRepository quizMetadataRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuizImportService(ObjectMapper objectMapper, QuizMetaDataRepository quizMetadataRepository, TopicRepository topicRepository, QuestionRepository questionRepository) {
        this.objectMapper = objectMapper;
        this.quizMetadataRepository = quizMetadataRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    private static String createTopicId(String subject, String book, String title, String topicName, String description) {
        return StringUtils.join(List.of(subject, book, title, topicName, description), "_");
    }

    public Optional<QuizData> importQuizFile(byte[] jsonFileBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(jsonFileBytes)) {
            // Use Jackson Streaming API for memory efficiency
            JsonParser parser = objectMapper.getFactory().createParser(inputStream);
            QuizData quizData = new QuizData();
            List<Question> questions = new ArrayList<>();
            List<Topic> topics = new ArrayList<>();
            int batchSize = 1000;

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == null) break;

                if (JsonToken.FIELD_NAME.equals(token)) {
                    String fieldName = parser.getCurrentName();

                    if ("quiz_metadata".equals(fieldName)) {
                        parser.nextToken();
                        QuizMetadata quizMetadata = objectMapper.readValue(parser, QuizMetadata.class);
                        quizMetadata.createAndSetId();
                        quizData.setQuizMetadata(quizMetadata);
                    }

                    if ("topics".equals(fieldName)) {
                        parser.nextToken();
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                Topic topic = objectMapper.readValue(parser, Topic.class);
                                topic.setQuizId(quizData.getQuizMetadata().getId());
                                topics.add(topic);
                            }
                        }
                        quizData.setTopics(topics);
                    }

                    if ("questions".equals(fieldName)) {
                        parser.nextToken(); // START_ARRAY
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                Question q = objectMapper.readValue(parser, Question.class);
                                questions.add(q);
                            }
                        }
                        quizData.setQuestions(questions);
                    }
                }
            }

            // Establish Topic-Question relationships
            establishTopicQuestionRelationships(quizData);

            return Optional.of(quizData);
        } catch (IOException e) {
            LOG.error("Error while importing quiz file", e);
            return Optional.empty();
        }
    }

    @Transactional
    public void persist(Optional<QuizData> quizData) {
        quizData.ifPresent(data -> {
            QuizMetadata quizMetadata = data.getQuizMetadata();
            quizMetadataRepository.save(quizMetadata);
            List<Topic> topics = data.getTopics();
            topicRepository.saveAll(topics);
            List<Question> questions = data.getQuestions();
            questionRepository.saveAll(questions);
        });
    }

    /**
     * Establishes the @ManyToOne relationships between Questions and Topics
     * using the topicId field from JSON to find the corresponding Topic entity
     */
    private void establishTopicQuestionRelationships(QuizData quizData) {
        QuizMetadata quizMetadata = quizData.getQuizMetadata();
        List<Topic> topics = quizData.getTopics();
        List<Question> questions = quizData.getQuestions();

        if (topics == null || questions == null) {
            LOG.warn("Topics or questions list is null, skipping relationship mapping");
            return;
        }

        Map<String, Topic> topicLookup = topics.stream().collect(Collectors.toMap(Topic::getTopicId, Function.identity()));
        Map<String, String> topicIdHash = topics.stream().collect(Collectors.toMap(Topic::getTopicId, topic -> createTopicId(quizMetadata.getSubject(), quizMetadata.getBook(), quizMetadata.getTitle(), topic.getTopicName(), topic.getDescription())));


        // Establish Question -> Topic relationships
        for (Question question : questions) {
            String topicIdReplacing = question.getTopicId();
            if (topicIdHash.containsKey(topicIdReplacing)) {
                String newTopicId = topicIdHash.get(topicIdReplacing);

                Topic relatedTopic = topicLookup.get(topicIdReplacing);
                relatedTopic.setTopicId(newTopicId);

                question.setTopicId(newTopicId);
                question.setTopicRefId(newTopicId);
            } else {
                LOG.warn("No topic found for question {} with topicId {}", question.getId(), question.getTopicId());
            }
        }
    }
}
