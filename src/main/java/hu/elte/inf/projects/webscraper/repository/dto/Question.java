package hu.elte.inf.projects.webscraper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.List;

@Entity
public class Question {

    @Id
    private String id;

    @JsonProperty("topic_id")
    private String topicId;
    
    @JsonProperty("topic_name")
    private String topicName;
    
    private String difficulty;
    
    @JsonProperty("correct_answer")
    private int correctAnswer;

    private String question;

    private String explanation;

    @ElementCollection
    private List<String> options;

    @ElementCollection
    private List<String> keywords;

    @Column(name = "topic_ref_id")
    private String topicRefId;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getTopicRefId() {
        return topicRefId;
    }

    public void setTopicRefId(String topicRefId) {
        this.topicRefId = topicRefId;
    }

}
