package hu.elte.inf.projects.quizme.repository.dto;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "topics")
public class Topic {

    @Id
    private String id;

    @Field("topic_id")
    @JsonProperty("topic_id")
    private String topicId;

    @Field("topic_name")
    @JsonProperty("topic_name")
    private String topicName;

    private String alias;

    private String description;

    // Denormalized references for efficient querying
    @Field("title_name")
    private String titleName;

    @Field("sub_category_name")
    private String subCategoryName;

    @Field("category_name")
    private String categoryName;

    @Field("question_ids")
    private List<String> questionIds = new ArrayList<>(); // References to Question documents

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

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
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

    public List<String> getQuestionIds() {
        return Optional.ofNullable(questionIds).orElse(new ArrayList<>());
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}