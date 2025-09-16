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