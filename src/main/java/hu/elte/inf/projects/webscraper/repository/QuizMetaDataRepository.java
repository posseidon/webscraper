package hu.elte.inf.projects.webscraper.repository;

import hu.elte.inf.projects.webscraper.repository.dto.QuizMetadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizMetaDataRepository extends JpaRepository<QuizMetadata, String> {
    
    Optional<QuizMetadata> findByTitle(String title);
    
    @EntityGraph(value = "QuizMetadata.withTopics")
    Optional<QuizMetadata> findWithTopicsByTitle(String title);
    
    @EntityGraph(value = "QuizMetadata.withTopics")
    Optional<QuizMetadata> findWithTopicsById(String id);
    
    @Query("SELECT DISTINCT q.category FROM QuizMetadata q ORDER BY q.topic")
    List<String> findAllDistinctCategories();

    @Query("SELECT DISTINCT q.topic FROM QuizMetadata q WHERE q.category = :subject ORDER BY q.topic")
    List<String> findDistinctTopicsByCategory(@Param("subject") String subject);
    
    @Query("SELECT q FROM QuizMetadata q WHERE q.category = :subject AND q.topic = :book ORDER BY q.title")
    List<QuizMetadata> findByCategoryAndTopic(@Param("subject") String subject, @Param("book") String book);

    Optional<QuizMetadata> findByCategoryAndTopicAndTitle(String subject, String book, String title);
}
