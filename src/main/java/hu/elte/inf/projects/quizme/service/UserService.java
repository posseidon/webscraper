package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.dto.QuizData;

import java.util.List;

public interface UserService {
    void saveOrUpdateUser(String email, String name);
    List<QuizData> findUserQuizProgress(String email); // Placeholder
}
