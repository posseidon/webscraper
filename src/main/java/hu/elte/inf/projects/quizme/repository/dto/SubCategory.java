package hu.elte.inf.projects.quizme.repository.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subcategories")
public class SubCategory {

    @Id
    private String id;
    private String name;
    private String alias;
    private String categoryName;

    private List<Title> titles = new ArrayList<>();

    public SubCategory(String name) {
        this.name = name;
    }

    public SubCategory() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
        // Set back reference
        titles.forEach(title -> title.setSubCategoryName(this.name));
    }

    public void addTitle(Title title) {
        titles.add(title);
        title.setSubCategoryName(this.name);
        title.setCategoryName(this.categoryName);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
