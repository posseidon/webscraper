package hu.elte.inf.projects.quizme;

import hu.elte.inf.projects.quizme.repository.TopicRepository;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.QuizService;
import hu.elte.inf.projects.quizme.service.json.QuizImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class QuizServiceIntegrationTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizImportService quizImportService;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    @Transactional
    public void testFindQuestionsByTopicId_ShouldLoadQuestionsCorrectly() throws IOException {
        // Import test data
        String jsonFilePath = "src/test/resources/json/1.json";
        byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFilePath));
        Optional<QuizData> quizDataOpt = quizImportService.importQuizFile(jsonBytes);
        assertTrue(quizDataOpt.isPresent(), "Quiz data should be imported");
        
        quizImportService.persist(quizDataOpt);

        // Find a topic by topicId
        String testTopicId = "nemzeti_jelképek"; // From the JSON file
        Optional<Topic> topicOpt = topicRepository.findByTopicId(testTopicId);
        
        assertTrue(topicOpt.isPresent(), "Topic should be found");
        Topic topic = topicOpt.get();
        
        System.out.println("Topic ID (database): " + topic.getId());
        System.out.println("Topic ID (topicId field): " + topic.getTopicId());
        System.out.println("Topic name: " + topic.getTopicName());
        System.out.println("Questions count: " + topic.getQuestions().size());
        
        // Debug: Print all questions for this topic
        topic.getQuestions().forEach(q -> {
            System.out.println("Question ID: " + q.getId() + 
                             ", Topic ID: " + q.getTopicId() + 
                             ", Question: " + q.getQuestion());
        });

        // Test the service method
        List<Question> questions = quizService.findQuestionsByTopicId(testTopicId);
        
        System.out.println("Questions found by service: " + questions.size());
        
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
    @Transactional
    public void testTopicQuestionsRelationship_DirectCheck() throws IOException {
        // Import test data
        String jsonFilePath = "src/test/resources/json/1.json";
        byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonFilePath));
        Optional<QuizData> quizDataOpt = quizImportService.importQuizFile(jsonBytes);
        assertTrue(quizDataOpt.isPresent(), "Quiz data should be imported");
        
        quizImportService.persist(quizDataOpt);

        // Get all topics and check their questions
        List<Topic> allTopics = topicRepository.findAll();
        
        System.out.println("Total topics found: " + allTopics.size());
        
        for (Topic topic : allTopics) {
            System.out.println("\nTopic: " + topic.getTopicId() + " (" + topic.getTopicName() + ")");
            System.out.println("Database ID: " + topic.getId());
            System.out.println("Questions count: " + topic.getQuestions().size());
            
            if (topic.getQuestions().isEmpty()) {
                System.out.println("WARNING: No questions found for topic: " + topic.getTopicId());
            }
        }
        
        // Find topic with questions
        Optional<Topic> topicWithQuestions = allTopics.stream()
                .filter(t -> !t.getQuestions().isEmpty())
                .findFirst();
        
        assertTrue(topicWithQuestions.isPresent(), "At least one topic should have questions");
    }
}