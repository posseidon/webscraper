package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.dto.*;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

}