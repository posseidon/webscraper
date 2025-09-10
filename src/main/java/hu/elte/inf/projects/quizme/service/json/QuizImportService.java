package hu.elte.inf.projects.quizme.service.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.inf.projects.quizme.repository.*;
import hu.elte.inf.projects.quizme.repository.dto.*;
import hu.elte.inf.projects.quizme.service.SequenceService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizImportService {
    private static final Logger LOG = LoggerFactory.getLogger(QuizImportService.class);

    private final ObjectMapper objectMapper;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final SequenceService sequenceService;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TitleRepository titleRepository;

    @Autowired
    public QuizImportService(
            ObjectMapper objectMapper,
            TopicRepository topicRepository,
            QuestionRepository questionRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            TitleRepository titleRepository,
            SequenceService sequenceService) {
        this.objectMapper = objectMapper;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.titleRepository = titleRepository;
        this.sequenceService = sequenceService;
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

                        String categoryName = quizMetadata.getCategory();
                        String subCategoryName = quizMetadata.getSubCategory();
                        String titleName = quizMetadata.getTitle();

                        if(StringUtils.isNotBlank(quizMetadata.getCategory())){
                            // create new Category unless it already exists
                            Category category = categoryRepository.findByName(quizMetadata.getCategory()).stream().findFirst().orElse(new Category(categoryName));
                            category = categoryRepository.save(category);
                            
                            if(StringUtils.isNotBlank(subCategoryName)){
                                SubCategory subCategory = subCategoryRepository.findByName(subCategoryName).stream().findFirst().orElse(new SubCategory(subCategoryName));
                                subCategory.setCategory(category);
                                subCategory = subCategoryRepository.save(subCategory);

                                if(StringUtils.isNotBlank(titleName)){
                                    Title title = titleRepository.findByName(titleName).stream().findFirst().orElse(new Title(titleName));
                                    title.setDescription(quizMetadata.getDescription());
                                    title.setCreatedDate(quizMetadata.getCreatedDate());
                                    title.setLearningObjectives(quizMetadata.getLearningObjectives());
                                    title.setStudyTips(quizMetadata.getStudyTips());
                                    title.setLanguage(quizMetadata.getLanguage());
                                    title.setTotalQuestions(quizMetadata.getTotalQuestions());
                                    title.setVersion(quizMetadata.getVersion());
                                    title.setSubCategory(subCategory);
                                    
                                    titleRepository.save(title);
                                }
                            }
                        }
                    }

                    if ("topics".equals(fieldName)) {
                        parser.nextToken();
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            Title title = titleRepository.findByName(quizData.getQuizMetadata().getTitle()).get(0);
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                Topic topic = objectMapper.readValue(parser, Topic.class);
                                topic.setId(UUID.randomUUID().toString());
                                topic.setTitle(title);

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
                                q.setId(UUID.randomUUID().toString());
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
            // Save topics first
            List<Topic> topics = data.getTopics();
            topicRepository.saveAll(topics);
            
            // Now establish the actual object relationships before saving questions
            establishTopicObjectRelationships(data);
            
            // Save questions (they now have proper topic references)
            List<Question> questions = data.getQuestions();
            questionRepository.saveAll(questions);
        });
    }
    
    /**
     * Sets up the actual Topic object references on Questions after Topics are saved
     */
    private void establishTopicObjectRelationships(QuizData quizData) {
        List<Topic> topics = quizData.getTopics();
        List<Question> questions = quizData.getQuestions();

        if (topics == null || questions == null) {
            LOG.warn("Topics or questions list is null, skipping object relationship mapping");
            return;
        }

        Map<String, Topic> topicLookup = topics.stream().collect(Collectors.toMap(Topic::getTopicId, Function.identity()));

        // Set the actual Topic object references
        for (Question question : questions) {
            String questionTopicId = question.getTopicId();
            if (questionTopicId != null && topicLookup.containsKey(questionTopicId)) {
                Topic relatedTopic = topicLookup.get(questionTopicId);
                relatedTopic.getQuestions().add(question);
                question.setTopic(relatedTopic);
            } else {
                LOG.warn("No topic found for question {} with topicId {}", question.getId(), questionTopicId);
            }
        }
    }

    /**
     * Establishes the topic name mapping for questions
     * Object relationships will be resolved by JPA after saving
     */
    private void establishTopicQuestionRelationships(QuizData quizData) {
        List<Topic> topics = quizData.getTopics();
        List<Question> questions = quizData.getQuestions();

        if (topics == null || questions == null) {
            LOG.warn("Topics or questions list is null, skipping relationship mapping");
            return;
        }

        Map<String, Topic> topicLookup = topics.stream().collect(Collectors.toMap(Topic::getTopicId, Function.identity()));

        // Only set topic names - don't set object relationships yet
        for (Question question : questions) {
            String questionTopicId = question.getTopicId();
            if (questionTopicId != null && topicLookup.containsKey(questionTopicId)) {
                Topic relatedTopic = topicLookup.get(questionTopicId);
                question.setTopicName(relatedTopic.getTopicName());
                // Don't set question.setTopic() here - let JPA resolve it after saving
            } else {
                LOG.warn("No topic found for question {} with topicId {}", question.getId(), questionTopicId);
            }
        }
    }
}
