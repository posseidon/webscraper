package hu.elte.inf.projects.quizme.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import hu.elte.inf.projects.quizme.repository.QuestionRepository;
import hu.elte.inf.projects.quizme.repository.SequenceRepository;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.Sequence;

@Service
public class SequenceService {
    private static final Logger LOG = LoggerFactory.getLogger(SequenceService.class);
    private static final String QUESTION_ID_SEQUENCE = "question_id";

    private final SequenceRepository sequenceRepository;
    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;

    public SequenceService(SequenceRepository sequenceRepository, QuestionRepository questionRepository,
            MongoTemplate mongoTemplate) {
        this.sequenceRepository = sequenceRepository;
        this.questionRepository = questionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public String getNextQuestionId() {
        try {
            // Try to increment existing sequence
            Sequence sequence = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("_id").is(QUESTION_ID_SEQUENCE)),
                    new Update().inc("value", 1),
                    FindAndModifyOptions.options().returnNew(true).upsert(true),
                    Sequence.class);

            if (sequence == null) {
                // if sequence is null, it means it was just created, so we need to initialize
                // it
                initializeQuestionSequence();
                sequence = mongoTemplate.findAndModify(
                        Query.query(Criteria.where("_id").is(QUESTION_ID_SEQUENCE)),
                        new Update().inc("value", 1),
                        FindAndModifyOptions.options().returnNew(true).upsert(true),
                        Sequence.class);
            }

            return generateQuestionId(sequence.getValue());

        } catch (Exception e) {
            LOG.error("Error generating next question ID", e);
            throw new RuntimeException("Failed to generate question ID", e);
        }
    }

    private void initializeQuestionSequence() {
        // Initialize sequence starting from 101 (or higher based on existing data)
        Long initialValue = findMaxQuestionIdFromDatabase();
        Sequence sequence = new Sequence(QUESTION_ID_SEQUENCE, initialValue);
        sequenceRepository.save(sequence);
        LOG.info("Initialized question ID sequence with value: {}", initialValue);
    }

    private Long findMaxQuestionIdFromDatabase() {
        try {
            List<Question> questions = questionRepository.findAll();
            long maxId = 0L;

            for (Question question : questions) {
                String id = question.getId();
                if (id != null && id.startsWith("Q") && id.length() >= 4) {
                    try {
                        long numericId = Long.parseLong(id.substring(1));
                        maxId = Math.max(maxId, numericId);
                    } catch (NumberFormatException e) {
                        LOG.warn("Invalid question ID format: {}", id);
                    }
                }
            }

            LOG.info("Found max existing question ID: {}", generateQuestionId(maxId));
            return maxId; // Return the max found, will be incremented in sequence

        } catch (Exception e) {
            LOG.warn("Error finding max question ID from database, defaulting to 100", e);
            return 100L;
        }
    }

    /**
     * Generate Question ID with flexible formatting that supports unlimited
     * numbers.
     * For values 1-999: Q001, Q002, ..., Q999 (maintains existing format)
     * For values 1000+: Q1000, Q1001, ..., Q9999, Q10000, etc. (no padding limit)
     */
    private String generateQuestionId(Long value) {
        if (value <= 999) {
            // Use 3-digit zero-padding for compatibility with existing data (Q001-Q999)
            return String.format("Q%03d", value);
        } else {
            // No padding for values >= 1000, allowing unlimited growth (Q1000, Q1001, etc.)
            return "Q" + value;
        }
    }
}