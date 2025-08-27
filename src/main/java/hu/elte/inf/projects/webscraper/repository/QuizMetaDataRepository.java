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
    
    @Query("SELECT DISTINCT q.subject FROM QuizMetadata q ORDER BY q.book")
    List<String> findAllDistinctSubjects();
    
    @Query("SELECT DISTINCT q.book FROM QuizMetadata q WHERE q.subject = :subject ORDER BY q.book")
    List<String> findDistinctBooksBySubject(@Param("subject") String subject);
    
    @Query("SELECT q FROM QuizMetadata q WHERE q.subject = :subject AND q.book = :book ORDER BY q.title")
    List<QuizMetadata> findBySubjectAndBook(@Param("subject") String subject, @Param("book") String book);
    
    Optional<QuizMetadata> findBySubjectAndBookAndTitle(String subject, String book, String title);
}
