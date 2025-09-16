package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.SubCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SubCategoryRepository extends MongoRepository<SubCategory, String> {
    List<SubCategory> findByName(String name);

    List<SubCategory> findByCategoryName(String categoryName);
}
