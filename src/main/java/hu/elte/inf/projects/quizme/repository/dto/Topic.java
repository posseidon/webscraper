package hu.elte.inf.projects.quizme.repository.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Topic {

    @Id
    private String id;

    @JsonProperty("topic_id")
    private String topicId;

    @JsonProperty("topic_name")
    private String topicName;

    @Column(length = 10000, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "title_id")
    private Title title;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_topic_id")
    private List<Question> questions;


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Question> getQuestions() {
        return Optional.ofNullable(questions).orElse(new ArrayList<>());
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }
}