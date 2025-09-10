package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, String> {
    List<SubCategory> findByName(String name);

    List<SubCategory> findByCategory_Name(String categoryName);
}
