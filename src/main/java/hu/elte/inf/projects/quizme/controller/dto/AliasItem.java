package hu.elte.inf.projects.quizme.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// @JsonInclude helps in ignoring null fields if you were to send this object as a response
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliasItem {
    private String category;
    private String sub_category;
    private String title;
    private String alias;

    // Standard Getters and Setters for all fields

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSub_category() {
        return sub_category;
    }

    public void setSub_category(String sub_category) {
        this.sub_category = sub_category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "TranslationItem{" +
                "category='" + category + '\'' +
                ", sub_category='" + sub_category + '\'' +
                ", title='" + title + '\'' +
                ", alias='" + alias + '\'' +
                '}';
    }
}
