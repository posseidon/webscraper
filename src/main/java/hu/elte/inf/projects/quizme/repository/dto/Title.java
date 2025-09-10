package hu.elte.inf.projects.quizme.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Title {

    @Id
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "title_id")
    private List<Topic> topics;

    @ElementCollection
    @JsonProperty("learning_objectives")
    private List<String> learningObjectives;

    @ElementCollection
    @JsonProperty("study_tips")
    private List<String> studyTips;

    private String description;

    @JsonProperty("total_questions")
    private int totalQuestions;

    private String language;

    @JsonProperty("created_date")
    private LocalDate createdDate;

    private String version;

    public Title() {}
    public Title(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public void addTopic(Topic topic){
        this.topics.add(topic);
        topic.setTitle(this);
    }

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

}
