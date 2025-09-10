package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TitleRepository extends JpaRepository<Title, String> {
    List<Title> findByName(String name);
}
