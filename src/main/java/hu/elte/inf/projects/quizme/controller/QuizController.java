package hu.elte.inf.projects.quizme.controller;

import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.Title;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.JsonDifficultyService;
import hu.elte.inf.projects.quizme.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import static hu.elte.inf.projects.quizme.util.QuizConstants.*;

@Controller
public class QuizController {

    private final QuizService quizService;
    private final JsonDifficultyService difficultyService;

    public QuizController(QuizService quizService, JsonDifficultyService difficultyService) {
        this.quizService = quizService;
        this.difficultyService = difficultyService;
    }

    @GetMapping(ROOT)
    public String home(Model model) {
        List<String> categories = quizService.findAllDistinctCategories();
        model.addAttribute(ATTR_CATEGORIES, categories);
        return VIEW_LANDING;
    }

    @GetMapping(QUIZ_CATEGORIES)
    public String showSubjects(Model model) {
        List<String> categories = quizService.findAllDistinctCategories();
        model.addAttribute(ATTR_CATEGORIES, categories);

        return VIEW_CATEGORIES;
    }

    @GetMapping(USER_MANUAL)
    public String showUserManual() {
        return VIEW_USER_MANUAL;
    }

    @GetMapping(QUIZ_CATEGORY)
    public String showSubCategories(@PathVariable String category, Model model) {
        List<String> subCategories = quizService.findDistinctSubCategories(category);
        model.addAttribute(ATTR_SUBCATEGORIES, subCategories);
        model.addAttribute(ATTR_CATEGORY, category);
        return VIEW_SUBCATEGORIES;
    }

    @GetMapping(QUIZ_CATEGORY_SUB)
    public String showTitles(@PathVariable String category, @PathVariable String subcategory, Model model) {
        List<Title> titles = quizService.findTitlesByCategoryAndSubCategory(category, subcategory);
        model.addAttribute(ATTR_TITLES, titles);
        model.addAttribute(ATTR_CATEGORY, category);
        model.addAttribute(ATTR_SUBCATEGORY, subcategory);
        return VIEW_TITLES;
    }

    @GetMapping(QUIZ_CATEGORY_SUB_TITLE)
    public String showTopics(@PathVariable String category, @PathVariable String subcategory,
            @PathVariable String title, Model model) {
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

        model.addAttribute(ATTR_TOPICS, topics);
        model.addAttribute(ATTR_TITLE, title);
        model.addAttribute(ATTR_TITLE_OBJECT, titleObject);
        model.addAttribute(ATTR_CATEGORY, category);
        model.addAttribute(ATTR_SUBCATEGORY, subcategory);

        return VIEW_TOPICS;
    }

    @PostMapping(QUIZ_PLAY)
    public String playTopicQuiz(@PathVariable String topicId, Model model) {
        List<Question> questions = quizService.findQuestionsByTopicId(topicId);

        if (questions.isEmpty()) {
            return REDIRECT + QUIZ_CATEGORIES + REDIRECT_ERROR_PARAM + ERROR_NO_QUESTIONS;
        }

        questions.forEach(question -> question.setTopic(null));

        model.addAttribute(ATTR_QUESTIONS, questions);
        model.addAttribute(ATTR_TOTAL, questions.size());
        model.addAttribute(ATTR_SELECTED_COUNT, questions.size());
        model.addAttribute(ATTR_SELECTED_DIFFICULTY, DIFFICULTY_MIXED);
        model.addAttribute(ATTR_QUIZ_TITLE, topicId);

        return VIEW_QUIZ_PLAY;
    }

    @PostMapping(QUIZ_START)
    public String startQuiz(@PathVariable String titleId,
            @RequestParam(name = PARAM_QUESTION_COUNT, defaultValue = "0") int questionCount,
            @RequestParam(name = PARAM_DIFFICULTY, defaultValue = DIFFICULTY_MIXED) String difficulty,
            Model model) {
        List<Question> questions = quizService.findQuestionsByTitle(titleId);
        if (!CollectionUtils.isEmpty(questions)) {
            List<Question> balancedQuestions = selectBalancedQuestions(questions, questionCount, DIFFICULTY_MIXED);

            balancedQuestions.forEach(question -> question.setTopic(null));

            model.addAttribute(ATTR_QUESTIONS, balancedQuestions);
            model.addAttribute(ATTR_TOTAL, balancedQuestions.size());
            model.addAttribute(ATTR_SELECTED_COUNT, questionCount);
            model.addAttribute(ATTR_SELECTED_DIFFICULTY, difficulty);
            model.addAttribute(ATTR_QUIZ_TITLE, titleId);
            return VIEW_QUIZ_PLAY;
        }
        return REDIRECT + QUIZ_CATEGORIES + REDIRECT_ERROR_PARAM + ERROR_QUIZ_NOT_FOUND;
    }

    private List<Question> selectBalancedQuestions(List<Question> questions, int requestedCount, String difficulty) {
        // Special handling for "mixed" difficulty - distribute evenly across all
        // difficulty levels
        if (DIFFICULTY_MIXED.equalsIgnoreCase(difficulty)) {
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

        // If no limit specified or limit >= total, return all filtered questions
        // shuffled
        if (requestedCount <= 0 || requestedCount >= allQuestions.size()) {
            Collections.shuffle(allQuestions);
            return allQuestions;
        }

        // Calculate questions per subcategory (ensure each subcategory gets at least 1
        // question)
        Map<String, List<Question>> questionsByTopicName = questions.stream()
                .filter(q -> difficultyService.matchesDifficulty(q.getDifficulty(), difficulty))
                .collect(Collectors.groupingBy(Question::getTopicName));

        int questionsPerTopic = Math.max(1, requestedCount / questionsByTopicName.size());
        int remainder = requestedCount % questionsByTopicName.size();

        List<Question> selectedQuestions = new ArrayList<>();

        // Select questions from each subcategory
        for (Map.Entry<String, List<Question>> entry : questionsByTopicName.entrySet()) {
            List<Question> questionsList = entry.getValue();
            Collections.shuffle(questionsList);
            int questionsFromThisTopic = questionsPerTopic + (questionsList.size() < remainder ? 1 : 0);
            questionsFromThisTopic = Math.min(questionsFromThisTopic, questionsList.size());

            selectedQuestions.addAll(questionsList.subList(0, questionsFromThisTopic));
        }

        // If we still need more questions (some topics had fewer questions), fill from
        // remaining
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
        List<String> difficultyLevels = DIFFICULTY_LEVELS;
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

            if (questionsForLevel == null || questionsForLevel.isEmpty()) {
                continue;
            }

            Collections.shuffle(questionsForLevel);

            // Add extra question to some levels if there's remainder
            int questionsFromThisLevel = questionsPerDifficulty + (i < remainder ? 1 : 0);
            questionsFromThisLevel = Math.min(questionsFromThisLevel, questionsForLevel.size());

            selectedQuestions.addAll(questionsForLevel.subList(0, questionsFromThisLevel));
        }

        // If we still need more questions (some levels had fewer questions), fill from
        // remaining
        if (selectedQuestions.size() < requestedCount) {
            List<Question> remaining = new ArrayList<>();
            for (String level : difficultyLevels) {
                List<Question> levelQuestions = questionsByDifficultyLevel.get(level);
                if (levelQuestions != null) {
                    remaining.addAll(levelQuestions.stream()
                            .filter(q -> !selectedQuestions.contains(q))
                            .toList());
                }
            }
            Collections.shuffle(remaining);

            int needed = requestedCount - selectedQuestions.size();
            selectedQuestions.addAll(remaining.subList(0, Math.min(needed, remaining.size())));
        }

        // Final shuffle to mix difficulty levels
        Collections.shuffle(selectedQuestions);
        return selectedQuestions;
    }

    @PostMapping(QUIZ_SUBMIT_RESULTS)
    @ResponseBody
    public Map<String, Object> submitQuizResults(@RequestBody Map<String, Object> results) {

        Map<String, Object> response = new HashMap<>();
        response.put(RESULT_SUCCESS, true);
        response.put(RESULT_MESSAGE, SUCCESS_RESULTS_MESSAGE);
        response.put(RESULT_SCORE, results.get(RESULT_CORRECT) + "/" + results.get(RESULT_TOTAL));

        return response;
    }

    @PutMapping("/titles/audio-overview")
    @ResponseBody
    public ResponseEntity<String> updateTitleAudioOverview(@RequestBody List<TitleAudioOverviewUpdateRequest> updates) {
        for (TitleAudioOverviewUpdateRequest update : updates) {
            try {
                quizService.updateTitleAudioOverview(update.getTitleName(), update.getAudioOverview());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            } catch (MalformedURLException e) {
                return new ResponseEntity<>(
                        "Invalid URL for title " + update.getTitleName() + ": " + update.getAudioOverview(),
                        HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Title audio overviews updated successfully!", HttpStatus.OK);
    }
}
