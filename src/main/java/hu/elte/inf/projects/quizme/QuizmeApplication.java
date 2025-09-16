package hu.elte.inf.projects.quizme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableMongoRepositories(basePackages = "hu.elte.inf.projects.quizme.repository")
@SpringBootApplication
public class QuizmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizmeApplication.class, args);
    }

}
