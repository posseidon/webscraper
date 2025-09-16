package hu.elte.inf.projects.quizme;

import hu.elte.inf.projects.quizme.service.SequenceService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite:file:memorydb_test?mode=memory&cache=shared")
public class SequenceServiceTest {

    @Test
    public void testQuestionIdGeneration() throws Exception {
        SequenceService sequenceService = new SequenceService(null, null, null);
        
        // Use reflection to test the private generateQuestionId method
        Method generateQuestionId = SequenceService.class.getDeclaredMethod("generateQuestionId", Long.class);
        generateQuestionId.setAccessible(true);
        
        // Test 3-digit padding for values <= 999
        assertEquals("Q001", generateQuestionId.invoke(sequenceService, 1L));
        assertEquals("Q010", generateQuestionId.invoke(sequenceService, 10L));
        assertEquals("Q100", generateQuestionId.invoke(sequenceService, 100L));
        assertEquals("Q999", generateQuestionId.invoke(sequenceService, 999L));
        
        // Test unlimited format for values > 999
        assertEquals("Q1000", generateQuestionId.invoke(sequenceService, 1000L));
        assertEquals("Q1001", generateQuestionId.invoke(sequenceService, 1001L));
        assertEquals("Q9999", generateQuestionId.invoke(sequenceService, 9999L));
        assertEquals("Q10000", generateQuestionId.invoke(sequenceService, 10000L));
        assertEquals("Q999999", generateQuestionId.invoke(sequenceService, 999999L));
        
        // Test very large numbers (no theoretical limit)
        assertEquals("Q1000000", generateQuestionId.invoke(sequenceService, 1000000L));
        assertEquals("Q" + Long.MAX_VALUE, generateQuestionId.invoke(sequenceService, Long.MAX_VALUE));
    }
}