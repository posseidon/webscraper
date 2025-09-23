package hu.elte.inf.projects.quizme.service;

import hu.elte.inf.projects.quizme.repository.UserRepository;
import hu.elte.inf.projects.quizme.repository.dto.User;
import hu.elte.inf.projects.quizme.repository.dto.QuizData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void saveOrUpdateUser(String email, String name) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(name);
        } else {
            user = new User();
            user.setEmail(email);
            user.setName(name);
        }
        userRepository.save(user);
    }

    @Override
    public List<QuizData> findUserQuizProgress(String email) {
        // This is a placeholder implementation.
        // You would need to implement the logic to retrieve quiz progress for the user.
        // This might involve another repository call.
        return Collections.emptyList();
    }
}
