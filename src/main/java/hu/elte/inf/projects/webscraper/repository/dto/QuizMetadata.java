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

    private String book;
    private String title;
    private String subject;
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
        if(Objects.nonNull(this.book) && Objects.nonNull(this.title) && Objects.nonNull(this.subject)){
            String id = StringUtils.join(List.of(this.subject, this.book, this.title), "_");
            setId(id);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
