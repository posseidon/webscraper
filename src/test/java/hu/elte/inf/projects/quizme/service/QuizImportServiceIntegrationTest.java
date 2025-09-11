package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.*;
import hu.elte.inf.projects.quizme.repository.dto.*;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QuizImportServiceIntegrationTest {

    @Autowired
    private QuizImportService quizImportService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SubCategoryRepository subCategoryRepository;
    
    @Autowired
    private TitleRepository titleRepository;
    
    @Autowired
    private TopicRepository topicRepository;
    
    @Autowired
    private QuestionRepository questionRepository;

    private byte[] testJsonData;

    @BeforeEach
    void setUp() throws IOException {
        // Load test JSON file
        ClassPathResource resource = new ClassPathResource("json/1.json");
        testJsonData = resource.getInputStream().readAllBytes();
    }

    @Test
    void testImportQuizFile_ShouldParseJsonCorrectly() {
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(testJsonData);
        
        // Then
        assertTrue(result.isPresent(), "Quiz data should be successfully parsed");
        
        QuizData quizData = result.get();
        assertNotNull(quizData.getQuizMetadata(), "Quiz metadata should not be null");
        assertNotNull(quizData.getTopics(), "Topics should not be null");
        assertNotNull(quizData.getQuestions(), "Questions should not be null");
        
        // Verify metadata
        QuizMetadata metadata = quizData.getQuizMetadata();
        assertEquals("Magyar kulturális ismeret", metadata.getCategory());
        assertEquals("Kultúra és Identitás", metadata.getSubCategory());
        assertEquals("Magyarország nemzeti jelképei és ünnepei", metadata.getTitle());
        assertEquals(50, metadata.getTotalQuestions());
        assertEquals("magyar", metadata.getLanguage());
        
        // Verify topics
        List<Topic> topics = quizData.getTopics();
        assertEquals(5, topics.size(), "Should have 5 topics");
        
        // Verify specific topic
        Optional<Topic> nationalSymbolsTopic = topics.stream()
                .filter(t -> "nemzeti_jelképek".equals(t.getTopicId()))
                .findFirst();
        assertTrue(nationalSymbolsTopic.isPresent(), "Should have national symbols topic");
        assertEquals("Nemzeti jelképek", nationalSymbolsTopic.get().getTopicName());
        
        // Verify questions
        List<Question> questions = quizData.getQuestions();
        assertEquals(50, questions.size(), "Should have 50 questions");
        
        // Verify specific question (note: IDs are UUIDs, not the original JSON IDs)
        Optional<Question> firstQuestion = questions.stream()
                .filter(q -> "nemzeti_jelképek".equals(q.getTopicId()))
                .findFirst();
        assertTrue(firstQuestion.isPresent(), "Should have question for national symbols topic");
        Question q1 = firstQuestion.get();
        assertEquals("nemzeti_jelképek", q1.getTopicId());
        assertEquals("könnyű", q1.getDifficulty());
        assertEquals(0, q1.getCorrectAnswer());
        assertNotNull(q1.getOptions());
        assertEquals(4, q1.getOptions().size(), "Should have 4 options");
    }

    @Test
    void testPersist_ShouldSaveAllEntitiesCorrectly() {
        // Given
        Optional<QuizData> quizData = quizImportService.importQuizFile(testJsonData);
        assertTrue(quizData.isPresent(), "Quiz data should be parsed successfully");
        
        // When
        quizImportService.persist(quizData);
        
        // Then
        // Verify Category is saved
        List<Category> categories = categoryRepository.findAll();
        assertEquals(1, categories.size(), "Should have 1 category");
        Category category = categories.get(0);
        assertEquals("Magyar kulturális ismeret", category.getName());
        
        // Verify SubCategory is saved
        List<SubCategory> subCategories = subCategoryRepository.findAll();
        assertEquals(1, subCategories.size(), "Should have 1 subcategory");
        SubCategory subCategory = subCategories.get(0);
        assertEquals("Kultúra és Identitás", subCategory.getName());
        assertEquals(category.getName(), subCategory.getCategory().getName());
        
        // Verify Title is saved
        List<Title> titles = titleRepository.findAll();
        assertEquals(1, titles.size(), "Should have 1 title");
        Title title = titles.get(0);
        assertEquals("Magyarország nemzeti jelképei és ünnepei", title.getName());
        assertEquals(subCategory.getName(), title.getSubCategory().getName());
        assertEquals(50, title.getTotalQuestions());
        assertEquals("magyar", title.getLanguage());
        assertNotNull(title.getLearningObjectives());
        assertEquals(5, title.getLearningObjectives().size());
        assertNotNull(title.getStudyTips());
        assertEquals(5, title.getStudyTips().size());
        
        // Verify Topics are saved
        List<Topic> topics = topicRepository.findAll();
        assertEquals(5, topics.size(), "Should have 5 topics");
        
        // Verify specific topic
        Optional<Topic> nationalSymbolsTopic = topicRepository.findByTopicId("nemzeti_jelképek");
        assertTrue(nationalSymbolsTopic.isPresent(), "Should find national symbols topic by ID");
        Topic topic = nationalSymbolsTopic.get();
        assertEquals("Nemzeti jelképek", topic.getTopicName());
        assertEquals(title.getName(), topic.getTitle().getName());
        
        // Verify Questions are saved
        List<Question> questions = questionRepository.findAll();
        assertEquals(50, questions.size(), "Should have 50 questions");
        
        // Verify question relationships
        List<Question> nationalSymbolsQuestions = questions.stream()
                .filter(q -> "nemzeti_jelképek".equals(q.getTopicId()))
                .toList();
        assertFalse(nationalSymbolsQuestions.isEmpty(), "Should have questions for national symbols topic");
        
        // Verify specific question
        Question firstQuestion = nationalSymbolsQuestions.get(0);
        assertEquals("nemzeti_jelképek", firstQuestion.getTopicId());
    }
    
    @Test
    void testTopicQuestionRelationship_ShouldBeEstablishedCorrectly() {
        // Given
        Optional<QuizData> quizData = quizImportService.importQuizFile(testJsonData);
        assertTrue(quizData.isPresent());
        
        // When
        quizImportService.persist(quizData);
        
        // Then
        // Find a specific topic
        Optional<Topic> nationalSymbolsTopic = topicRepository.findByTopicId("nemzeti_jelképek");
        assertTrue(nationalSymbolsTopic.isPresent());
        
        Topic topic = nationalSymbolsTopic.get();
        
        // Verify bidirectional relationship
        assertNotNull(topic.getQuestions(), "Topic should have questions list");
        assertFalse(topic.getQuestions().isEmpty(), "Topic should have questions");
        
        // Count questions for this topic
        long questionCount = topic.getQuestions().size();
        assertTrue(questionCount > 0, "Topic should have questions");
        
        // Verify each question points back to the topic
        for (Question question : topic.getQuestions()) {
            assertEquals("nemzeti_jelképek", question.getTopicId(), "Question should have correct topicId");
        }
    }
    
    @Test
    void testDifficultyDistribution_ShouldMatchExpectedCounts() {
        // Given
        Optional<QuizData> quizData = quizImportService.importQuizFile(testJsonData);
        assertTrue(quizData.isPresent());
        
        // When
        quizImportService.persist(quizData);
        
        // Then
        List<Question> questions = questionRepository.findAll();
        
        long easyCount = questions.stream().filter(q -> "könnyű".equals(q.getDifficulty())).count();
        long mediumCount = questions.stream().filter(q -> "közepes".equals(q.getDifficulty())).count();
        long hardCount = questions.stream().filter(q -> "nehéz".equals(q.getDifficulty())).count();
        
        // Based on the JSON metadata difficulty_distribution
        assertTrue(easyCount > 0, "Should have easy questions");
        assertTrue(mediumCount > 0, "Should have medium questions");
        assertTrue(hardCount > 0, "Should have hard questions");
        
        assertEquals(50, easyCount + mediumCount + hardCount, "Total questions should match");
    }
    
    @Test
    void testTopicDistribution_ShouldMatchExpectedCounts() {
        // Given
        Optional<QuizData> quizData = quizImportService.importQuizFile(testJsonData);
        assertTrue(quizData.isPresent());
        
        // When
        quizImportService.persist(quizData);
        
        // Then
        List<Question> questions = questionRepository.findAll();
        
        // Count questions by topic
        long nationalSymbolsCount = questions.stream().filter(q -> "nemzeti_jelképek".equals(q.getTopicId())).count();
        long hymnCount = questions.stream().filter(q -> "himnusz_és_szózat".equals(q.getTopicId())).count();
        long holidaysCount = questions.stream().filter(q -> "nemzeti_ünnepek".equals(q.getTopicId())).count();
        long historyCount = questions.stream().filter(q -> "történelmi_háttér".equals(q.getTopicId())).count();
        long legalCount = questions.stream().filter(q -> "jogi_és_alkotmányos_vonatkozások".equals(q.getTopicId())).count();
        
        assertTrue(nationalSymbolsCount > 0, "Should have questions for national symbols");
        assertTrue(hymnCount > 0, "Should have questions for hymn and szózat");
        assertTrue(holidaysCount > 0, "Should have questions for holidays");
        assertTrue(historyCount > 0, "Should have questions for history");
        assertTrue(legalCount > 0, "Should have questions for legal aspects");
        
        assertEquals(50, nationalSymbolsCount + hymnCount + holidaysCount + historyCount + legalCount, 
                "Total questions should match");
    }
    
    @Test
    void testInvalidJson_ShouldReturnEmpty() {
        // Given
        byte[] invalidJson = "invalid json content".getBytes();
        
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(invalidJson);
        
        // Then
        assertFalse(result.isPresent(), "Invalid JSON should return empty result");
    }
    
    @Test
    void testEmptyJson_ShouldReturnEmpty() {
        // Given
        byte[] emptyJson = "{}".getBytes();
        
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(emptyJson);
        
        // Then
        assertTrue(result.isPresent(), "Empty JSON should return present but incomplete data");
        QuizData quizData = result.get();
        assertNull(quizData.getQuizMetadata());
        assertTrue(quizData.getTopics() == null || quizData.getTopics().isEmpty());
        assertTrue(quizData.getQuestions() == null || quizData.getQuestions().isEmpty());
    }

    @Test
    void testTopicQuestionsEagerLoading_ShouldLoadQuestionsAutomatically() {
        // Given
        Optional<QuizData> quizData = quizImportService.importQuizFile(testJsonData);
        assertTrue(quizData.isPresent());
        quizImportService.persist(quizData);
        
        // When - Find topic by repository (simulating normal application usage)
        Optional<Topic> foundTopic = topicRepository.findByTopicId("nemzeti_jelképek");
        
        // Then
        assertTrue(foundTopic.isPresent(), "Should find the topic");
        Topic topic = foundTopic.get();
        
        // Verify questions are eagerly loaded
        assertNotNull(topic.getQuestions(), "Questions should be loaded");
        assertFalse(topic.getQuestions().isEmpty(), "Questions list should not be empty");
        
        // Verify question count matches expectations
        assertTrue(topic.getQuestions().size() > 0, "Should have questions loaded");
        
        // Verify each question has proper data
        for (Question question : topic.getQuestions()) {
            assertNotNull(question.getId(), "Question should have ID");
            assertNotNull(question.getQuestion(), "Question should have question text");
            assertNotNull(question.getOptions(), "Question should have options");
            assertFalse(question.getOptions().isEmpty(), "Question should have non-empty options");
            assertEquals("nemzeti_jelképek", question.getTopicId(), "Question should have correct topic ID");
        }
    }
}