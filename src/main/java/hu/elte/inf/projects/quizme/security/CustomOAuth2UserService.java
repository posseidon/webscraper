package hu.elte.inf.projects.quizme.security;

import hu.elte.inf.projects.quizme.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("CustomOAuth2UserService.loadUser is called");
        OAuth2User oauth2User = super.loadUser(userRequest);

        // Here you can save user to database, update last login, etc.
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        logger.info("Saving or updating user: email={}, name={}", email, name);
        // Save or update user in your database
        userService.saveOrUpdateUser(email, name);

        return oauth2User;
    }
}