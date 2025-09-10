package hu.elte.inf.projects.quizme.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.Title;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.JsonDifficultyService;
import hu.elte.inf.projects.quizme.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final JsonDifficultyService difficultyService;
    private final ObjectMapper objectMapper;

    @Autowired
    public QuizController(QuizService quizService, JsonDifficultyService difficultyService, ObjectMapper objectMapper) {
        this.quizService = quizService;
        this.difficultyService = difficultyService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<String> categories = quizService.findAllDistinctCategories();
        model.addAttribute("categories", categories);
        return "landing_page";
    }

    @GetMapping("/quiz/categories")
    public String showSubjects(Model model) {
        List<String> categories = quizService.findAllDistinctCategories();
        model.addAttribute("categories", categories);

        return "categories";
    }

    @GetMapping("/user-manual")
    public String showUserManual() {
        return "user-manual";
    }

    @GetMapping("/quiz/{category}")
    public String showSubCategories(@PathVariable String category, Model model) {
        List<String> subCategories = quizService.findDistinctSubCategories(category);
        model.addAttribute("subCategories", subCategories);
        model.addAttribute("category", category);
        return "subcategories";
    }

    @GetMapping("/quiz/{category}/{subcategory}")
    public String showTitles(@PathVariable String category, @PathVariable String subcategory, Model model) {
        List<Title> titles = quizService.findTitlesByCategoryAndSubCategory(category, subcategory);
        model.addAttribute("titles", titles);
        model.addAttribute("category", category);
        model.addAttribute("subcategory", subcategory);
        return "titles";
    }

    @GetMapping("/quiz/{category}/{subcategory}/{title}")
    public String showTopics(@PathVariable String category, @PathVariable String subcategory, @PathVariable String title, Model model){
        List<Topic> topics = quizService.findTopicsTitleByName(title);
        
        // Get the full Title object for the modal
        Title titleObject = quizService.findTitleByName(title);
        
        // Calculate total questions if not set
        if (titleObject.getTotalQuestions() == 0 && !topics.isEmpty()) {
            int totalQuestions = topics.stream()
                .mapToInt(topic -> topic.getQuestions() != null ? topic.getQuestions().size() : 0)
                .sum();
            titleObject.setTotalQuestions(totalQuestions);
        }

        model.addAttribute("topics", topics);
        model.addAttribute("title", title);
        model.addAttribute("titleObject", titleObject);
        model.addAttribute("category", category);
        model.addAttribute("subcategory", subcategory);

        return "topics";
    }

    @PostMapping("/quiz/play/{topicId}")
    public String playTopicQuiz(@PathVariable String topicId, Model model){
        List<Question> questions = quizService.findQuestionsByTopicId(topicId);
        
        if (questions.isEmpty()) {
            return "redirect:/quiz/categories?error=No questions found for this topic";
        }

        questions.forEach(question -> question.setTopic(null));

        model.addAttribute("questions", questions);
        model.addAttribute("total", questions.size());
        model.addAttribute("selectedCount", questions.size());
        model.addAttribute("selectedDifficulty", "mixed");
        model.addAttribute("quizTitle", topicId);

        return "quiz-play";
    }

    @PostMapping("/quiz/start/{titleId}")
    public String startQuiz(@PathVariable String titleId,
                           @RequestParam(name = "questionCount", defaultValue = "0") int questionCount,
                           @RequestParam(name = "difficulty", defaultValue = "mixed") String difficulty,
                           Model model) {
        List<Question> questions = quizService.findQuestionsByTitle(titleId);
        if (!CollectionUtils.isEmpty(questions)) {
            List<Question> balancedQuestions = selectBalancedQuestions(questions, questionCount, "mixed");

            balancedQuestions.forEach(question -> question.setTopic(null));

            model.addAttribute("questions", balancedQuestions);
            model.addAttribute("total", balancedQuestions.size());
            model.addAttribute("selectedCount", questionCount);
            model.addAttribute("selectedDifficulty", difficulty);
            model.addAttribute("quizTitle", titleId);
            return "quiz-play";
        }
        return "redirect:/quiz/categories?error=Quiz not found";
    }
    
    private List<Question> selectBalancedQuestions(List<Question> questions, int requestedCount, String difficulty) {
        // Special handling for "mixed" difficulty - distribute evenly across all difficulty levels
        if ("mixed".equalsIgnoreCase(difficulty)) {
            return selectMixedDifficultyQuestions(questions, requestedCount);
        }
        
        // Get all questions filtered by difficulty and set subcategory names
        List<Question> allQuestions = questions.stream()
                .filter(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty))
                .collect(Collectors.toList());
        
        // If no questions match the difficulty, fall back to all questions
        if (allQuestions.isEmpty()) {
            allQuestions = questions;
        }
        
        // If no limit specified or limit >= total, return all filtered questions shuffled
        if (requestedCount <= 0 || requestedCount >= allQuestions.size()) {
            Collections.shuffle(allQuestions);
            return allQuestions;
        }
        
        // Calculate questions per subcategory (ensure each subcategory gets at least 1 question)
        Map<String, List<Question>> questionsByTopicName = questions.stream()
                .filter(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty))
                .collect(Collectors.groupingBy(Question::getTopicName));
        
        int questionsPerTopic = Math.max(1, requestedCount / questionsByTopicName.size());
        int remainder = requestedCount % questionsByTopicName.size();
        
        List<Question> selectedQuestions = new ArrayList<>();
        
        // Select questions from each subcategory
        for(Map.Entry<String, List<Question>> entry : questionsByTopicName.entrySet()) {
            List<Question> questionsList = entry.getValue();
            Collections.shuffle(questionsList);
            int questionsFromThisTopic = questionsPerTopic + (questionsList.size() < remainder ? 1 : 0);
            questionsFromThisTopic = Math.min(questionsFromThisTopic, questionsList.size());

            selectedQuestions.addAll(questionsList.subList(0, questionsFromThisTopic));
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
    
    private List<Question> selectMixedDifficultyQuestions(List<Question> questions, int requestedCount) {
        List<String> difficultyLevels = Arrays.asList("könnyű", "közepes", "nehéz");
        Map<String, List<Question>> questionsByDifficultyLevel = questions.stream()
                .collect(Collectors.groupingBy(Question::getDifficulty));
        
        // Calculate total available questions
        int totalAvailable = questionsByDifficultyLevel.values().stream().mapToInt(List::size).sum();
        
        // If no limit specified or limit >= total, return all questions shuffled
        if (requestedCount <= 0 || requestedCount >= totalAvailable) {
            List<Question> allQuestions = questionsByDifficultyLevel.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            Collections.shuffle(allQuestions);
            return allQuestions;
        }
        
        // Distribute questions evenly across difficulty levels
        List<Question> selectedQuestions = new ArrayList<>();
        int questionsPerDifficulty = requestedCount / questionsByDifficultyLevel.values().size();
        int remainder = requestedCount % questionsByDifficultyLevel.size();
        
        for (int i = 0; i < difficultyLevels.size(); i++) {
            String level = difficultyLevels.get(i);
            List<Question> questionsForLevel = questionsByDifficultyLevel.get(level);
            
            if (questionsForLevel.isEmpty()) {
                continue;
            }
            
            Collections.shuffle(questionsForLevel);
            
            // Add extra question to some levels if there's remainder
            int questionsFromThisLevel = questionsPerDifficulty + (i < remainder ? 1 : 0);
            questionsFromThisLevel = Math.min(questionsFromThisLevel, questionsForLevel.size());
            
            selectedQuestions.addAll(questionsForLevel.subList(0, questionsFromThisLevel));
        }
        
        // If we still need more questions (some levels had fewer questions), fill from remaining
        if (selectedQuestions.size() < requestedCount) {
            List<Question> remaining = new ArrayList<>();
            for (String level : difficultyLevels) {
                List<Question> levelQuestions = questionsByDifficultyLevel.get(level);
                remaining.addAll(levelQuestions.stream()
                        .filter(q -> !selectedQuestions.contains(q))
                        .collect(Collectors.toList()));
            }
            Collections.shuffle(remaining);
            
            int needed = requestedCount - selectedQuestions.size();
            selectedQuestions.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }
        
        // Final shuffle to mix difficulty levels
        Collections.shuffle(selectedQuestions);
        return selectedQuestions;
    }


    @GetMapping("/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("app", "quizme");
        return response;
    }
    
    @GetMapping("/debug/duplicates/{topicId}")
    @ResponseBody
    public Map<String, Object> checkDuplicates(@PathVariable String topicId) {
        Map<String, Object> response = new HashMap<>();
        List<hu.elte.inf.projects.quizme.repository.dto.Topic> duplicateTopics = 
                quizService.findTopicsTitleByName(topicId); // This will help us debug
        
        response.put("topicId", topicId);
        response.put("duplicateCount", duplicateTopics.size());
        
        return response;
    }

    @PostMapping("/quiz/start/topic/{topicId}")
    public String startTopicQuiz(@PathVariable String topicId,
                                @RequestParam(name = "questionCount", defaultValue = "0") int questionCount,
                                @RequestParam(name = "difficulty", defaultValue = "mixed") String difficulty,
                                Model model) {
        List<Question> questions = quizService.findQuestionsByTopicId(topicId);
        if (!CollectionUtils.isEmpty(questions)) {
            List<Question> balancedQuestions = selectBalancedQuestions(questions, questionCount, difficulty);
            
            // Get topic name for display
            String topicName = questions.get(0).getTopicName();
            
            model.addAttribute("questions", balancedQuestions);
            model.addAttribute("total", balancedQuestions.size());
            model.addAttribute("selectedCount", questionCount);
            model.addAttribute("selectedDifficulty", difficulty);
            model.addAttribute("quizTitle", topicName);
            return "quiz-play";
        }
        return "redirect:/quiz/categories?error=Topic quiz not found";
    }

    @PostMapping("/quiz/start/topic/{topicId}/quick")
    public String startTopicQuizQuick(@PathVariable String topicId, Model model) {
        List<Question> questions = quizService.findQuestionsByTopicId(topicId);
        if (!CollectionUtils.isEmpty(questions)) {
            // Use default settings: mixed difficulty, all questions (up to 50)
            int maxQuestions = Math.min(questions.size(), 50);
            List<Question> balancedQuestions = selectBalancedQuestions(questions, maxQuestions, "mixed");
            
            // Get topic name for display
            String topicName = questions.get(0).getTopicName();
            
            model.addAttribute("questions", balancedQuestions);
            model.addAttribute("total", balancedQuestions.size());
            model.addAttribute("selectedCount", maxQuestions);
            model.addAttribute("selectedDifficulty", "mixed");
            model.addAttribute("quizTitle", topicName);
            return "quiz-play";
        }
        return "redirect:/quiz/categories?error=Topic quiz not found";
    }

    @PostMapping("/quiz/submit-results")
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
