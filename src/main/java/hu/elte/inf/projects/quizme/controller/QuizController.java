package hu.elte.inf.projects.quizme.controller;

import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_CATEGORIES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_CATEGORY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_QUESTIONS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_QUIZ_TITLE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_SELECTED_COUNT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_SELECTED_DIFFICULTY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_SUBCATEGORIES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_SUBCATEGORY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TITLE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TITLES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TITLE_OBJECT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TOPICS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TOTAL;
import static hu.elte.inf.projects.quizme.util.QuizConstants.DIFFICULTY_LEVELS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.DIFFICULTY_MIXED;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ERROR_NO_QUESTIONS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ERROR_QUIZ_NOT_FOUND;
import static hu.elte.inf.projects.quizme.util.QuizConstants.PARAM_DIFFICULTY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.PARAM_QUESTION_COUNT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_CATEGORIES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_CATEGORY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_CATEGORY_SUB;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_CATEGORY_SUB_TITLE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_FORM;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_PLAY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_START;
import static hu.elte.inf.projects.quizme.util.QuizConstants.QUIZ_SUBMIT_RESULTS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.REDIRECT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.REDIRECT_ERROR_PARAM;
import static hu.elte.inf.projects.quizme.util.QuizConstants.RESULT_CORRECT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.RESULT_MESSAGE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.RESULT_SCORE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.RESULT_SUCCESS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.RESULT_TOTAL;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ROOT;
import static hu.elte.inf.projects.quizme.util.QuizConstants.SUCCESS_RESULTS_MESSAGE;
import static hu.elte.inf.projects.quizme.util.QuizConstants.USER_MANUAL;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_CATEGORIES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_LANDING;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_QUIZ_FORM;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_QUIZ_PLAY;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_SUBCATEGORIES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_TITLES;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_TOPICS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.VIEW_USER_MANUAL;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TITLE_ALIAS;
import static hu.elte.inf.projects.quizme.util.QuizConstants.ATTR_TOPIC_ALIAS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.elte.inf.projects.quizme.repository.dto.Category;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.SubCategory;
import hu.elte.inf.projects.quizme.repository.dto.Title;
import hu.elte.inf.projects.quizme.repository.dto.Topic;
import hu.elte.inf.projects.quizme.service.JsonDifficultyService;
import hu.elte.inf.projects.quizme.service.QuizService;

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
        List<Category> categories = quizService.findAllDistinctCategories();
        model.addAttribute(ATTR_CATEGORIES, categories);
        return VIEW_LANDING;
    }

    @GetMapping(QUIZ_CATEGORIES)
    public String showSubjects(Model model) {
        List<Category> categories = quizService.findAllDistinctCategories();
        model.addAttribute(ATTR_CATEGORIES, categories);

        return VIEW_CATEGORIES;
    }

    @GetMapping(USER_MANUAL)
    public String showUserManual() {
        return VIEW_USER_MANUAL;
    }

    @GetMapping(QUIZ_CATEGORY)
    public String showSubCategories(@PathVariable String category, Model model) {
        List<SubCategory> subCategories = quizService.findDistinctSubCategories(category);
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
        List<Question> questions = quizService.findQuestionsByTitle(title);
        titleObject.setTotalQuestions(questions.size());

        model.addAttribute(ATTR_TOPICS, topics);
        model.addAttribute(ATTR_TITLE, title);
        model.addAttribute(ATTR_TITLE_ALIAS, titleObject.getAlias());
        model.addAttribute(ATTR_TITLE_OBJECT, titleObject);
        model.addAttribute(ATTR_CATEGORY, category);
        model.addAttribute(ATTR_SUBCATEGORY, subcategory);

        return VIEW_TOPICS;
    }

    @GetMapping(QUIZ_PLAY)
    public String playTopicQuiz(@PathVariable String topicId,
            @RequestParam(required = true) String title,
            @RequestParam(required = true) String category,
            @RequestParam(required = true) String subcategory,
            Model model) {
        Topic topic = quizService.findTopicById(topicId);

        if (topic == null || topic.getQuestionIds().isEmpty()) {
            return REDIRECT + QUIZ_CATEGORIES + REDIRECT_ERROR_PARAM + ERROR_NO_QUESTIONS;
        }

        List<Question> questions = quizService.getQuestionsByTopic(topic.getTopicId());
        if (CollectionUtils.isEmpty(questions)) {
            return REDIRECT + QUIZ_CATEGORIES + REDIRECT_ERROR_PARAM + ERROR_NO_QUESTIONS;
        }

        model.addAttribute(ATTR_QUESTIONS, questions);
        model.addAttribute(ATTR_TOTAL, questions.size());
        model.addAttribute(ATTR_SELECTED_COUNT, questions.size());
        model.addAttribute(ATTR_SELECTED_DIFFICULTY, DIFFICULTY_MIXED);
        model.addAttribute(ATTR_QUIZ_TITLE, topic.getTopicName());
        model.addAttribute(ATTR_TOPIC_ALIAS, topic.getAlias());
        model.addAttribute(ATTR_TITLE, title);
        model.addAttribute(ATTR_CATEGORY, category);
        model.addAttribute(ATTR_SUBCATEGORY, subcategory);

        return VIEW_QUIZ_PLAY;
    }

    @GetMapping(QUIZ_FORM)
    public String quizForm(@PathVariable String titleName, Model model) {
        Title title = quizService.findTitleByName(titleName);
        if (Objects.nonNull(title)) {
            model.addAttribute(ATTR_TITLE_OBJECT, title);
            model.addAttribute(ATTR_TITLE, title.getName());
            model.addAttribute(ATTR_CATEGORY, title.getCategoryName());
            model.addAttribute(ATTR_SUBCATEGORY, title.getSubCategoryName());
            return VIEW_QUIZ_FORM;
        }
        return REDIRECT + QUIZ_CATEGORIES + REDIRECT_ERROR_PARAM + ERROR_QUIZ_NOT_FOUND;
    }

    @PostMapping(QUIZ_START)
    public String startQuiz(@PathVariable String titleId,
            @RequestParam(name = PARAM_QUESTION_COUNT, defaultValue = "0") int questionCount,
            @RequestParam(name = PARAM_DIFFICULTY, defaultValue = DIFFICULTY_MIXED) String difficulty,
            Model model) {
        Title title = quizService.findTitleByName(titleId);
        List<Question> questions = quizService.findQuestionsByTitle(titleId);
        if (!CollectionUtils.isEmpty(questions)) {
            List<Question> balancedQuestions = selectBalancedQuestions(questions, questionCount, difficulty);

            model.addAttribute(ATTR_QUESTIONS, balancedQuestions);
            model.addAttribute(ATTR_TOTAL, balancedQuestions.size());
            model.addAttribute(ATTR_SELECTED_COUNT, questionCount);
            model.addAttribute(ATTR_SELECTED_DIFFICULTY, difficulty);
            model.addAttribute(ATTR_QUIZ_TITLE, title.getName());
            Optional<SubCategory> subCategory = quizService.findSubCategoryByName(title.getSubCategoryName());
            if (subCategory.isPresent()) {
                model.addAttribute(ATTR_TITLE, title);
                model.addAttribute(ATTR_SUBCATEGORY, subCategory.get().getName());
                model.addAttribute(ATTR_CATEGORY, subCategory.get().getCategoryName());
            }
            model.addAttribute(ATTR_TITLE, titleId);
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
        Map<String, List<Question>> questionsByTopicName = allQuestions.stream()
                .collect(Collectors.groupingBy(Question::getTopicId));

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
}