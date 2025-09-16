package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.Title;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TitleRepository extends MongoRepository<Title, String> {
    List<Title> findByName(String name);

    List<Title> findBySubCategoryName(String subCategoryName);

    List<Title> findByCategoryName(String categoryName);

    List<Title> findByCategoryNameAndSubCategoryName(String categoryName, String subCategoryName);
}
