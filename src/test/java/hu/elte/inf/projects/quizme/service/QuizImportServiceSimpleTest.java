package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.dto.*;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class QuizImportServiceSimpleTest {

    @Autowired
    private QuizImportService quizImportService;

    private byte[] testJsonData;

    @BeforeEach
    void setUp() throws IOException {
        // Load test JSON file
        ClassPathResource resource = new ClassPathResource("json/1.json");
        testJsonData = resource.getInputStream().readAllBytes();
    }

    @Test
    void testImportQuizFile_ParsesJsonCorrectly() {
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(testJsonData);
        
        // Then
        assertTrue(result.isPresent(), "Quiz data should be successfully parsed");
        
        QuizData quizData = result.get();
        
        // Verify metadata
        assertNotNull(quizData.getQuizMetadata(), "Quiz metadata should not be null");
        QuizMetadata metadata = quizData.getQuizMetadata();
        assertEquals("Magyar kulturális ismeret", metadata.getCategory());
        assertEquals("Kultúra és Identitás", metadata.getSubCategory());
        assertEquals("Magyarország nemzeti jelképei és ünnepei", metadata.getTitle());
        assertEquals(50, metadata.getTotalQuestions());
        assertEquals("magyar", metadata.getLanguage());
        
        // Verify topics
        assertNotNull(quizData.getTopics(), "Topics should not be null");
        List<Topic> topics = quizData.getTopics();
        assertEquals(5, topics.size(), "Should have 5 topics");
        
        // Check specific topic
        Optional<Topic> nationalSymbolsTopic = topics.stream()
                .filter(t -> "nemzeti_jelképek".equals(t.getTopicId()))
                .findFirst();
        assertTrue(nationalSymbolsTopic.isPresent(), "Should have national symbols topic");
        assertEquals("Nemzeti jelképek", nationalSymbolsTopic.get().getTopicName());
        
        // Verify questions
        assertNotNull(quizData.getQuestions(), "Questions should not be null");
        List<Question> questions = quizData.getQuestions();
        assertEquals(50, questions.size(), "Should have 50 questions");
        
        // Check specific question
        Optional<Question> nationalSymbolQuestion = questions.stream()
                .filter(q -> "nemzeti_jelképek".equals(q.getTopicId()))
                .findFirst();
        assertTrue(nationalSymbolQuestion.isPresent(), "Should have question for national symbols topic");
        Question question = nationalSymbolQuestion.get();
        assertEquals("nemzeti_jelképek", question.getTopicId());
        assertNotNull(question.getDifficulty());
        assertNotNull(question.getOptions());
        assertEquals(4, question.getOptions().size(), "Should have 4 options");
        assertTrue(question.getCorrectAnswer() >= 0 && question.getCorrectAnswer() < 4, 
                "Correct answer should be valid option index");
    }

    @Test
    void testImportQuizFile_HandlesInvalidJson() {
        // Given
        byte[] invalidJson = "invalid json content".getBytes();
        
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(invalidJson);
        
        // Then
        assertFalse(result.isPresent(), "Invalid JSON should return empty result");
    }

    @Test
    void testImportQuizFile_HandlesEmptyJson() {
        // Given
        byte[] emptyJson = "{}".getBytes();
        
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(emptyJson);
        
        // Then
        assertTrue(result.isPresent(), "Empty JSON should return present but incomplete data");
        QuizData quizData = result.get();
        // Empty JSON will have null or empty collections
        assertTrue(quizData.getQuestions() == null || quizData.getQuestions().isEmpty());
        assertTrue(quizData.getTopics() == null || quizData.getTopics().isEmpty());
    }
    
    @Test 
    void testTopicQuestionRelationships_EstablishedCorrectly() {
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(testJsonData);
        
        // Then
        assertTrue(result.isPresent());
        QuizData quizData = result.get();
        
        List<Topic> topics = quizData.getTopics();
        List<Question> questions = quizData.getQuestions();
        
        // Verify that every question has a topicId that matches a topic
        for (Question question : questions) {
            String questionTopicId = question.getTopicId();
            assertNotNull(questionTopicId, "Question should have a topic ID");
            
            boolean topicExists = topics.stream()
                    .anyMatch(topic -> questionTopicId.equals(topic.getTopicId()));
            assertTrue(topicExists, 
                    "Question topic ID '" + questionTopicId + "' should match an existing topic");
        }
        
        // Verify that every topic has questions
        for (Topic topic : topics) {
            String topicId = topic.getTopicId();
            long questionCount = questions.stream()
                    .filter(q -> topicId.equals(q.getTopicId()))
                    .count();
            assertTrue(questionCount > 0, 
                    "Topic '" + topicId + "' should have at least one question");
        }
    }
    
    @Test
    void testDifficultyDistribution_HasAllLevels() {
        // When
        Optional<QuizData> result = quizImportService.importQuizFile(testJsonData);
        
        // Then
        assertTrue(result.isPresent());
        List<Question> questions = result.get().getQuestions();
        
        long easyCount = questions.stream()
                .filter(q -> "könnyű".equals(q.getDifficulty()))
                .count();
        long mediumCount = questions.stream()
                .filter(q -> "közepes".equals(q.getDifficulty()))
                .count();
        long hardCount = questions.stream()
                .filter(q -> "nehéz".equals(q.getDifficulty()))
                .count();
        
        assertTrue(easyCount > 0, "Should have easy questions");
        assertTrue(mediumCount > 0, "Should have medium questions");
        assertTrue(hardCount > 0, "Should have hard questions");
        assertEquals(50, easyCount + mediumCount + hardCount, "Total should be 50");
    }
}