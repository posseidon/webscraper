package hu.elte.inf.projects.quizme.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.elte.inf.projects.quizme.util.QuizConstants;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping(QuizConstants.ROOT)
    public String home(Model model) {
        return "redirect:/quiz/categories";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/my-data")
    public String myData() {
        return "my-data";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            model.addAttribute("name", oauth2User.getAttribute("name"));
            model.addAttribute("email", oauth2User.getAttribute("email"));
            model.addAttribute("picture", oauth2User.getAttribute("picture"));
        }
        return "dashboard";
    }

    @GetMapping("/user")
    @ResponseBody
    public Map<String, Object> user(Authentication authentication) {
        Map<String, Object> userInfo = new HashMap<>();

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            userInfo.put("name", oauth2User.getAttribute("name"));
            userInfo.put("email", oauth2User.getAttribute("email"));
            userInfo.put("picture", oauth2User.getAttribute("picture"));
        }

        return userInfo;
    }
}