package hu.elte.inf.projects.webscraper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.List;

@Entity
@NamedEntityGraph(
    name = "Topic.withQuestions",
    attributeNodes = @NamedAttributeNode("questions")
)
public class Topic {

    @Id
    @JsonProperty("topic_id")
    private String topicId;
    
    @JsonProperty("topic_name")
    private String topicName;

    @Column(length = 10000, columnDefinition = "TEXT")
    private String description;

    @Column(name = "quiz_id")
    private String quizId;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_ref_id")
    private List<Question> questions;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }
}
