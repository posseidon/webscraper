package hu.elte.inf.projects.quizme.repository.dto;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Category {

    @Id
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SubCategory> subCategories = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }

    public Category() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addSubCategory(SubCategory subCategory) {
        subCategories.add(subCategory);
        subCategory.setCategory(this);
    }

    public void setSubCategories(List<SubCategory> subCategories){
        this.subCategories.addAll(subCategories);
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }
}
