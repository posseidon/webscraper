package hu.elte.inf.projects.quizme.service.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.elte.inf.projects.quizme.repository.CategoryRepository;
import hu.elte.inf.projects.quizme.repository.QuestionRepository;
import hu.elte.inf.projects.quizme.repository.SubCategoryRepository;
import hu.elte.inf.projects.quizme.repository.TitleRepository;
import hu.elte.inf.projects.quizme.repository.TopicRepository;
import hu.elte.inf.projects.quizme.repository.dto.Category;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import hu.elte.inf.projects.quizme.repository.dto.QuizMetadata;
import hu.elte.inf.projects.quizme.repository.dto.SubCategory;
import hu.elte.inf.projects.quizme.repository.dto.Title;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.SequenceService;

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

    public Optional<QuizData> importQuizFile(byte[] jsonFileBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(jsonFileBytes)) {
            // Use Jackson Streaming API for memory efficiency
            JsonParser parser = objectMapper.getFactory().createParser(inputStream);
            QuizData quizData = new QuizData();
            List<Question> questions = new ArrayList<>();
            List<Topic> topics = new ArrayList<>();

            while (!parser.isClosed()) {
                JsonToken token = parser.nextToken();
                if (token == null)
                    break;

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
                                topics.add(topic);
                            }
                        }
                        quizData.setTopics(topics);
                    }
                    List<String> topicIds = quizData.getTopics().stream().map(Topic::getTopicId)
                            .collect(Collectors.toList());
                    List<Topic> existingTopics = topicRepository.findByTopicIdIn(topicIds);
                    existingTopics.forEach(t -> {
                        quizData.getTopics().removeIf(topic -> topic.getTopicId().equals(t.getTopicId()));
                    });

                    if ("questions".equals(fieldName)) {
                        parser.nextToken(); // START_ARRAY
                        if (parser.currentToken() == JsonToken.START_ARRAY) {
                            while (parser.nextToken() != JsonToken.END_ARRAY) {
                                Question q = objectMapper.readValue(parser, Question.class);
                                questions.add(q);
                            }
                        }
                        questionRepository.findByQuestionIn(questions.stream()
                                .map(Question::getQuestion)
                                .collect(Collectors.toList()))
                                .forEach(q -> questions.removeIf(qq -> qq.getQuestion().equals(q.getQuestion())));
                        quizData.setQuestions(questions);
                    }
                }
            }

            return Optional.of(quizData);
        } catch (IOException e) {
            LOG.error("Error while importing quiz file", e);
            return Optional.empty();
        }
    }

    public void persist(Optional<QuizData> quizData) {
        quizData.ifPresent(data -> {
            QuizMetadata quizMetadata = data.getQuizMetadata();
            String categoryName = quizMetadata.getCategory();
            String subCategoryName = quizMetadata.getSubCategory();
            String titleName = quizMetadata.getTitle();

            if (StringUtils.isNotBlank(quizMetadata.getCategory())) {
                // create new Category unless it already exists
                Category category = categoryRepository.findByName(categoryName).stream().findFirst()
                        .orElse(new Category(categoryName));
                category.setAlias(quizMetadata.getCategoryAlias());
                category = categoryRepository.save(category);

                if (StringUtils.isNotBlank(subCategoryName)) {
                    SubCategory subCategory = subCategoryRepository.findByName(subCategoryName).stream().findFirst()
                            .orElse(new SubCategory(subCategoryName));
                    subCategory.setAlias(quizMetadata.getSubCategoryAlias());
                    subCategory.setCategoryName(category.getName());
                    subCategory = subCategoryRepository.save(subCategory);

                    if (StringUtils.isNotBlank(titleName)) {
                        Title title = titleRepository.findByName(titleName).stream().findFirst()
                                .orElse(new Title(titleName));
                        title.setDescription(quizMetadata.getDescription());
                        title.setCreatedDate(quizMetadata.getCreatedDate());
                        title.setLearningObjectives(quizMetadata.getLearningObjectives());
                        title.setStudyTips(quizMetadata.getStudyTips());
                        title.setLanguage(quizMetadata.getLanguage());
                        title.setTotalQuestions(quizMetadata.getTotalQuestions());
                        title.setVersion(quizMetadata.getVersion());
                        title.setCategoryName(category.getName());
                        title.setSubCategoryName(subCategory.getName());
                        title.setAlias(quizMetadata.getTitleAlias());

                        titleRepository.save(title);
                        subCategory.addTitle(title);

                        data.getQuestions().forEach(question -> {
                            question.setId(sequenceService.getNextQuestionId());
                        });

                        data.getTopics().forEach(topic -> {
                            topic.setId(UUID.randomUUID().toString());
                            topic.setTitleName(titleName);

                            List<String> questionIds = data.getQuestions().stream()
                                    .filter(q -> q.getTopicId().equals(topic.getTopicId()))
                                    .map(Question::getId)
                                    .collect(Collectors.toList());

                            topic.setQuestionIds(questionIds);
                        });

                        topicRepository.saveAll(data.getTopics());
                        questionRepository.saveAll(data.getQuestions());
                    }
                }
            }
        });
    }
}
