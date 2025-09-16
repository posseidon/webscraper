package hu.elte.inf.projects.quizme.repository.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;

    @Field("sub_categories")
    private List<SubCategory> subCategories = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public Category() {
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

    public void addSubCategory(SubCategory subCategory) {
        subCategories.add(subCategory);
        subCategory.setName(this.name);
    }

    public void setSubCategories(List<SubCategory> subCategories) {
        this.subCategories = subCategories;
        // Ensure category reference is set
        subCategories.forEach(sub -> sub.setName(this.name));
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }
}
