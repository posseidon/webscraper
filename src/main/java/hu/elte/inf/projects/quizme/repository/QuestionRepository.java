package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {
}
