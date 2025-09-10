package hu.elte.inf.projects.quizme.repository.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Sequence {

    @Id
    private String name;
    
    private Long value;

    public Sequence() {}

    public Sequence(String name, Long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}