package hu.elte.inf.projects.webscraper.controller;

import hu.elte.inf.projects.webscraper.repository.QuizMetaDataRepository;
import hu.elte.inf.projects.webscraper.repository.dto.Question;
import hu.elte.inf.projects.webscraper.repository.dto.QuizMetadata;
import hu.elte.inf.projects.webscraper.repository.dto.Topic;
import hu.elte.inf.projects.webscraper.service.JsonDifficultyService;
import hu.elte.inf.projects.webscraper.service.json.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("quiz")
public class QuizController {

    private final QuizService quizService;
    private final QuizMetaDataRepository quizMetaDataRepository;
    private final JsonDifficultyService difficultyService;

    @Autowired
    public QuizController(QuizService quizService, QuizMetaDataRepository quizMetaDataRepository, JsonDifficultyService difficultyService) {
        this.quizService = quizService;
        this.quizMetaDataRepository = quizMetaDataRepository;
        this.difficultyService = difficultyService;
    }

    @GetMapping("/categories")
    public String showSubjects(Model model) {
        List<String> subjects = quizMetaDataRepository.findAllDistinctCategories();
        model.addAttribute("categories", subjects);
        return "categories";
    }

    @GetMapping("/{category}")
    public String showBooks(@PathVariable String category, Model model) {
        List<String> books = quizMetaDataRepository.findDistinctTopicsByCategory(category);
        model.addAttribute("topics", books);
        model.addAttribute("category", category);
        return "topics";
    }

    @GetMapping("/{category}/{topic}")
    public String showTitles(@PathVariable String category, @PathVariable String topic, Model model) {
        List<QuizMetadata> quizzes = quizMetaDataRepository.findByCategoryAndTopic(category, topic);
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("category", category);
        model.addAttribute("topic", topic);
        return "titles";
    }

    @PostMapping("/start/{quizId}")
    public String startQuiz(@PathVariable String quizId, 
                           @RequestParam(name = "questionCount", defaultValue = "0") int questionCount,
                           @RequestParam(name = "difficulty", defaultValue = "mixed") String difficulty,
                           Model model) {
        QuizMetadata quiz = quizService.findCompleteQuizById(quizId);
        if (quiz != null) {
            List<Question> questions = selectBalancedQuestions(quiz, questionCount, difficulty);
            
            model.addAttribute("questions", questions);
            model.addAttribute("total", questions.size());
            model.addAttribute("selectedCount", questionCount);
            model.addAttribute("selectedDifficulty", difficulty);
            return "quiz-play";
        }
        return "redirect:/quiz/categories?error=Quiz not found";
    }
    
    private List<Question> selectBalancedQuestions(QuizMetadata quiz, int requestedCount, String difficulty) {
        List<Topic> topics = quiz.getTopics();
        
        // Get all questions filtered by difficulty and set topic names
        List<Question> allQuestions = topics.stream()
                .flatMap(topic -> topic.getQuestions().stream()
                        .peek(question -> question.setTopicName(topic.getTopicName())))
                .filter(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty))
                .collect(Collectors.toList());
        
        // If no questions match the difficulty, fall back to all questions
        if (allQuestions.isEmpty()) {
            allQuestions = topics.stream()
                    .flatMap(topic -> topic.getQuestions().stream()
                            .peek(question -> question.setTopicName(topic.getTopicName())))
                    .collect(Collectors.toList());
        }
        
        // If no limit specified or limit >= total, return all filtered questions shuffled
        if (requestedCount <= 0 || requestedCount >= allQuestions.size()) {
            Collections.shuffle(allQuestions);
            return allQuestions;
        }
        
        // Calculate questions per topic (ensure each topic gets at least 1 question)
        List<Topic> topicsWithQuestions = topics.stream()
                .filter(topic -> topic.getQuestions().stream()
                        .anyMatch(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty)))
                .collect(Collectors.toList());
        
        // If no topics have questions of the requested difficulty, use all topics
        if (topicsWithQuestions.isEmpty()) {
            topicsWithQuestions = topics;
        }
        
        int questionsPerTopic = Math.max(1, requestedCount / topicsWithQuestions.size());
        int remainder = requestedCount % topicsWithQuestions.size();
        
        List<Question> selectedQuestions = new ArrayList<>();
        
        // Select questions from each topic
        for (int i = 0; i < topicsWithQuestions.size(); i++) {
            Topic topic = topicsWithQuestions.get(i);
            List<Question> topicQuestions = topic.getQuestions().stream()
                    .filter(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty))
                    .collect(Collectors.toList());
            
            // If no questions of requested difficulty in this topic, use all questions
            if (topicQuestions.isEmpty()) {
                topicQuestions = new ArrayList<>(topic.getQuestions());
            }
            
            // Set topic name on each question
            topicQuestions.forEach(question -> question.setTopicName(topic.getTopicName()));
            
            Collections.shuffle(topicQuestions);
            
            // Add extra question to some topics if there's remainder
            int questionsFromThisTopic = questionsPerTopic + (i < remainder ? 1 : 0);
            questionsFromThisTopic = Math.min(questionsFromThisTopic, topicQuestions.size());
            
            selectedQuestions.addAll(topicQuestions.subList(0, questionsFromThisTopic));
        }
        
        // If we still need more questions (some topics had fewer questions), fill from remaining
        if (selectedQuestions.size() < requestedCount) {
            List<Question> remaining = allQuestions.stream()
                    .filter(q -> !selectedQuestions.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remaining);
            
            int needed = requestedCount - selectedQuestions.size();
            selectedQuestions.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }
        
        // Final shuffle
        Collections.shuffle(selectedQuestions);
        return selectedQuestions;
    }


    @PostMapping("/submit-results")
    @ResponseBody
    public Map<String, Object> submitQuizResults(@RequestBody Map<String, Object> results) {
        // Here you could save results to database if needed
        // For now, we'll just log them and return success
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Quiz results submitted successfully");
        response.put("score", results.get("correctAnswers") + "/" + results.get("totalQuestions"));
        
        // Log the results (you could save to database here)
        System.out.println("Quiz completed - Score: " + results.get("correctAnswers") + "/" + results.get("totalQuestions"));
        
        return response;
    }


}
