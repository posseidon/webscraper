package hu.elte.inf.projects.webscraper.repository;

import hu.elte.inf.projects.webscraper.repository.dto.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {
}
