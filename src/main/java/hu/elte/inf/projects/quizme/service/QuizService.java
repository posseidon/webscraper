package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.CategoryRepository;
import hu.elte.inf.projects.quizme.repository.SubCategoryRepository;
import hu.elte.inf.projects.quizme.repository.TitleRepository;
import hu.elte.inf.projects.quizme.repository.TopicRepository;
import hu.elte.inf.projects.quizme.repository.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class QuizService {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final TitleRepository titleRepository;
    private final TopicRepository topicRepository;

    public QuizService(CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            TitleRepository titleRepository,
            TopicRepository topicRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.titleRepository = titleRepository;
        this.topicRepository = topicRepository;
    }

    public List<String> findAllDistinctCategories() {
        return categoryRepository.findAllDistinctCategoryNames();
    }

    public List<String> findDistinctSubCategories(String categoryName) {
        return subCategoryRepository.findByCategory_Name(categoryName).stream().map(SubCategory::getName).toList();
    }

    public List<Title> findTitlesByCategoryAndSubCategory(String categoryName, String subCategoryName) {
        return categoryRepository.findByName(categoryName).stream()
                .findFirst()
                .map(Category::getSubCategories)
                .orElseGet(List::of)
                .stream()
                .filter(sc -> sc.getName().equals(subCategoryName))
                .flatMap(sc -> sc.getTitles() != null ? sc.getTitles().stream() : Stream.<Title>empty())
                .toList();
    }

    public List<Question> findQuestionsByTitle(String titleId) {
        return titleRepository.findById(titleId)
                .map(Title::getTopics)
                .orElseGet(List::of)
                .stream()
                .map(Topic::getQuestions)
                .flatMap(List::stream)
                .toList();
    }

    public List<Topic> findTopicsTitleByName(String titleName) {
        List<Title> titles = titleRepository.findByName(titleName);
        if (CollectionUtils.isEmpty(titles))
            return List.of();
        return titles.get(0).getTopics();
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
        Optional<Topic> optTopic = topicRepository.findByTopicId(topicId);
        // Check for duplicates and warn about them
        return topicRepository.findByTopicId(topicId)
                .map(Topic::getQuestions)
                .orElseGet(List::of);
    }

    public Topic findTopicById(String topicId) {
        return topicRepository.findByTopicId(topicId).orElse(null);
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
}
