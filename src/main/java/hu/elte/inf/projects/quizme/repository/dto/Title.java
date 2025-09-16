package hu.elte.inf.projects.quizme.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "titles")
public class Title {

    @Id
    private String id;

    private String name;

    @Field("sub_category_name")
    private String subCategoryName; // Denormalized reference

    @Field("category_name")
    private String categoryName; // Denormalized reference

    @Field("topic_ids")
    private List<String> topicIds; // References to Topic documents

    @Field("learning_objectives")
    @JsonProperty("learning_objectives")
    private List<String> learningObjectives;

    @Field("study_tips")
    @JsonProperty("study_tips")
    private List<String> studyTips;

    private String description;

    @Field("total_questions")
    @JsonProperty("total_questions")
    private int totalQuestions;

    private String language;

    @Field("created_date")
    @JsonProperty("created_date")
    private LocalDate createdDate;

    private String version;

    @Field("audio_overview")
    private URL audioOverview;

    public Title() {
    }

    public Title(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<String> getTopicIds() {
        return topicIds;
    }

    public void setTopicIds(List<String> topicIds) {
        this.topicIds = topicIds;
    }

    // ... other getters and setters remain the same

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public URL getAudioOverview() {
        return audioOverview;
    }

    public void setAudioOverview(URL audioOverview) {
        this.audioOverview = audioOverview;
    }
}
