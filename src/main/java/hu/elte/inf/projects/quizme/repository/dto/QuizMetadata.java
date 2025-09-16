package hu.elte.inf.projects.quizme.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QuizMetadata {

    private String id;

    @JsonProperty("sub_category")
    private String subCategory;

    private String title;
    private String description;
    private String category;
    private String level;

    @JsonProperty("total_questions")
    private int totalQuestions;

    private String language;

    @JsonProperty("created_date")
    private LocalDate createdDate;

    private String version;

    @JsonProperty("learning_objectives")
    private List<String> learningObjectives;

    @JsonProperty("study_tips")
    private List<String> studyTips;

    private List<Topic> topics;

    public void createAndSetId() {
        if (Objects.nonNull(this.subCategory) && Objects.nonNull(this.title) && Objects.nonNull(this.category)) {
            setId(UUID.randomUUID().toString());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String book) {
        this.subCategory = book;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String subject) {
        this.category = subject;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<String> getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(List<String> learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public List<String> getStudyTips() {
        return studyTips;
    }

    public void setStudyTips(List<String> studyTips) {
        this.studyTips = studyTips;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
