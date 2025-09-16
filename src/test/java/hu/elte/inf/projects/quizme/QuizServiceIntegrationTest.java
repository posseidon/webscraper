package hu.elte.inf.projects.quizme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.QuizService;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;

@SpringBootTest
@ActiveProfiles("test")
public class QuizServiceIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizImportService quizImportService;

    @Test
    public void testFindQuestionsByTopicId_ShouldLoadQuestionsCorrectly() throws IOException {
        // Import test data
        String jsonFilePath = "src/test/resources/json/1.json";
        byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFilePath));
        Optional<QuizData> quizDataOpt = quizImportService.importQuizFile(jsonBytes);
        assertTrue(quizDataOpt.isPresent(), "Quiz data should be imported");

        quizImportService.persist(quizDataOpt);

        // Find a topic by topicId
        String testTopicId = "nemzeti_jelképek"; // From the JSON file
        Topic topic = quizService.findTopicById(testTopicId);

        assertNotNull(topic, "Topic should be found");

        // Test the service method
        List<Question> questions = quizService.getQuestionsByTopic(testTopicId);

        assertFalse(questions.isEmpty(), "Questions should be loaded for topic: " + testTopicId);

        // Verify questions belong to the correct topic
        questions.forEach(question -> {
            assertEquals(testTopicId, question.getTopicId(),
                    "Question should belong to topic: " + testTopicId);
        });

        // Based on the JSON file, "nemzeti_jelképek" should have several questions
        assertTrue(questions.size() >= 5, "Should have at least 5 questions for topic: " + testTopicId);
    }

    @Test
    public void testTopicQuestionsRelationship_DirectCheck() throws IOException {
        // Import test data
        String jsonFilePath = "src/test/resources/json/1.json";
        byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFilePath));
        Optional<QuizData> quizDataOpt = quizImportService.importQuizFile(jsonBytes);
        assertTrue(quizDataOpt.isPresent(), "Quiz data should be imported");

        quizImportService.persist(quizDataOpt);

        // Get all topics and check their questions
        List<Topic> allTopics = quizService.findTopicsTitleByName("Állampolgársági ismeretek");

        for (Topic topic : allTopics) {
            List<Question> questions = quizService.getQuestionsByTopic(topic.getId());
            assertTrue(!questions.isEmpty(), "At least one topic should have questions");
        }

    }
}