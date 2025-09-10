package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT DISTINCT c.name FROM Category c ORDER BY c.name")
    List<String> findAllDistinctCategoryNames();

    List<Category> findByName(String name);
}
