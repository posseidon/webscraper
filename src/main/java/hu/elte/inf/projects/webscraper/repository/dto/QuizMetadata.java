package hu.elte.inf.projects.webscraper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.sqlite.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@NamedEntityGraph(
    name = "QuizMetadata.withTopics",
    attributeNodes = @NamedAttributeNode("topics")
)
public class QuizMetadata {

    @Id
    private String id;

    private String topic;
    private String title;
    private String category;
    private String level;
    
    @JsonProperty("total_questions")
    private int totalQuestions;
    
    private String language;
    
    @JsonProperty("created_date")
    private LocalDate createdDate;
    
    private String version;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "quiz_id")
    private List<Topic> topics;

    public void createAndSetId(){
        if(Objects.nonNull(this.topic) && Objects.nonNull(this.title) && Objects.nonNull(this.category)){
            String id = StringUtils.join(List.of(this.category, this.topic, this.title), "_");
            setId(id);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String book) {
        this.topic = book;
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
}
