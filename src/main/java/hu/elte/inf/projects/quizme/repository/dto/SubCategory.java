package hu.elte.inf.projects.quizme.repository.dto;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
public class SubCategory {

    @Id
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_category_id")
    private List<Title> titles =new ArrayList<>();

    public SubCategory(String name){
        this.name = name;
    }

    public SubCategory() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category){
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public List<Title> getTitles() {
        return titles;
    }

    public void setTitles(List<Title> titles) {
        this.titles = titles;
    }

    public void addTitle(Title title){
        titles.add(title);
        title.setSubCategory(this);
    }
}
