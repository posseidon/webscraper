package hu.elte.inf.projects.quizme.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import hu.elte.inf.projects.quizme.repository.CategoryRepository;
import hu.elte.inf.projects.quizme.repository.QuestionRepository;
import hu.elte.inf.projects.quizme.repository.SubCategoryRepository;
import hu.elte.inf.projects.quizme.repository.TitleRepository;
import hu.elte.inf.projects.quizme.repository.TopicRepository;
import hu.elte.inf.projects.quizme.repository.dto.Category;
import hu.elte.inf.projects.quizme.repository.dto.Question;
import hu.elte.inf.projects.quizme.repository.dto.SubCategory;
import hu.elte.inf.projects.quizme.repository.dto.Title;
import hu.elte.inf.projects.quizme.repository.dto.Topic;

@Service
public class QuizService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TitleRepository titleRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    public QuizService(CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            TitleRepository titleRepository,
            TopicRepository topicRepository,
            QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.titleRepository = titleRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
    }

    public List<String> findAllDistinctCategories() {
        return categoryRepository.findAll().stream().map(Category::getName).toList();
    }

    public List<String> findDistinctSubCategories(String categoryName) {
        return subCategoryRepository.findByCategoryName(categoryName).stream().map(SubCategory::getName).toList();
    }

    public List<Title> findTitlesByCategoryAndSubCategory(String categoryName, String subCategoryName) {
        return titleRepository.findByCategoryNameAndSubCategoryName(categoryName, subCategoryName);
    }

    public List<Question> findQuestionsByTitle(String titleId) {
        List<String> questionIds = topicRepository.findByTitleName(titleId).stream()
                .map(Topic::getQuestionIds)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return questionRepository.findByIdIn(questionIds);
    }

    public List<Question> getQuestionsByTopic(String topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    public List<Topic> findTopicsTitleByName(String titleName) {
        return topicRepository.findByTitleName(titleName);
    }

    public Title findTitleByName(String titleName) {
        List<Title> titles = titleRepository.findByName(titleName);
        if (CollectionUtils.isEmpty(titles)) {
            // Return a default Title object with the name
            Title defaultTitle = new Title(titleName);
            defaultTitle.setTotalQuestions(0);
            return defaultTitle;
        }
        return titles.get(0);
    }

    public List<Question> findQuestionsByTopicId(String topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    public void updateTitleAudioOverview(String titleName, String audioOverviewUrl) throws MalformedURLException {
        List<Title> titles = titleRepository.findByName(titleName);
        if (CollectionUtils.isEmpty(titles)) {
            throw new IllegalArgumentException("Title not found: " + titleName);
        }
        Title title = titles.get(0); // Assuming titleName is unique or taking the first one
        title.setAudioOverview(new URL(audioOverviewUrl));
        titleRepository.save(title);
    }

    public Topic findByTopicName(String topicName) {
        return topicRepository.findByTopicName(topicName).stream().findFirst().orElse(null);
    }

    public Topic findTopicById(String testTopicId) {
        return topicRepository.findByTopicId(testTopicId).orElse(null);
    }
}
