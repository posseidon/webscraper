package hu.elte.inf.projects.quizme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableJpaRepositories(basePackages = "hu.elte.inf.projects.quizme.repository")
@SpringBootApplication
public class QuizmeApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizmeApplication.class, args);
    }

}
