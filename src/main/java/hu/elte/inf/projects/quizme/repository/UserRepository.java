package hu.elte.inf.projects.quizme.repository;

import hu.elte.inf.projects.quizme.repository.dto.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
