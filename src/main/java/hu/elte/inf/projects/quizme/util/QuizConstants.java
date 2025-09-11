package hu.elte.inf.projects.quizme.util;

import java.util.Arrays;
import java.util.List;

public final class QuizConstants {
    private QuizConstants() {}

    // Paths
    public static final String ROOT = "/";
    public static final String QUIZ_CATEGORIES = "/quiz/categories";
    public static final String USER_MANUAL = "/user-manual";
    public static final String QUIZ_CATEGORY = "/quiz/{category}";
    public static final String QUIZ_CATEGORY_SUB = "/quiz/{category}/{subcategory}";
    public static final String QUIZ_CATEGORY_SUB_TITLE = "/quiz/{category}/{subcategory}/{title}";
    public static final String QUIZ_PLAY = "/quiz/play/{topicId}";
    public static final String QUIZ_START = "/quiz/start/{titleId}";
    public static final String QUIZ_SUBMIT_RESULTS = "/quiz/submit-results";

    // View names
    public static final String VIEW_LANDING = "landing_page";
    public static final String VIEW_CATEGORIES = "categories";
    public static final String VIEW_USER_MANUAL = "user-manual";
    public static final String VIEW_SUBCATEGORIES = "subcategories";
    public static final String VIEW_TITLES = "titles";
    public static final String VIEW_TOPICS = "topics";
    public static final String VIEW_QUIZ_PLAY = "quiz-play";

    // Model attribute keys
    public static final String ATTR_CATEGORIES = "categories";
    public static final String ATTR_SUBCATEGORIES = "subCategories";
    public static final String ATTR_CATEGORY = "category";
    public static final String ATTR_SUBCATEGORY = "subcategory";
    public static final String ATTR_TITLES = "titles";
    public static final String ATTR_TOPICS = "topics";
    public static final String ATTR_TITLE = "title";
    public static final String ATTR_TITLE_OBJECT = "titleObject";
    public static final String ATTR_QUESTIONS = "questions";
    public static final String ATTR_TOTAL = "total";
    public static final String ATTR_SELECTED_COUNT = "selectedCount";
    public static final String ATTR_SELECTED_DIFFICULTY = "selectedDifficulty";
    public static final String ATTR_QUIZ_TITLE = "quizTitle";

    // Request params
    public static final String PARAM_QUESTION_COUNT = "questionCount";
    public static final String PARAM_DIFFICULTY = "difficulty";

    // Difficulty values
    public static final String DIFFICULTY_MIXED = "mixed";
    public static final String LEVEL_EASY = "könnyű";
    public static final String LEVEL_MEDIUM = "közepes";
    public static final String LEVEL_HARD = "nehéz";
    public static final List<String> DIFFICULTY_LEVELS = Arrays.asList(LEVEL_EASY, LEVEL_MEDIUM, LEVEL_HARD);

    // Messages
    public static final String ERROR_NO_QUESTIONS = "No questions found for this topic";
    public static final String ERROR_QUIZ_NOT_FOUND = "Quiz not found";
    public static final String SUCCESS_RESULTS_MESSAGE = "Quiz results submitted successfully";

    // Redirect prefixes
    public static final String REDIRECT = "redirect:";
    public static final String REDIRECT_ERROR_PARAM = "?error=";

    // Results JSON/map keys
    public static final String RESULT_SUCCESS = "success";
    public static final String RESULT_MESSAGE = "message";
    public static final String RESULT_SCORE = "score";
    public static final String RESULT_CORRECT = "correctAnswers";
    public static final String RESULT_TOTAL = "totalQuestions";
}
