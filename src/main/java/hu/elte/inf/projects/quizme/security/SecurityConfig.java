package hu.elte.inf.projects.quizme.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${app.security.enabled:true}")
        private boolean securityEnabled;

        private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
        private final CustomOAuth2UserService customOAuth2UserService;

        public SecurityConfig(CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                        CustomOAuth2UserService customOAuth2UserService) {
                this.authenticationSuccessHandler = authenticationSuccessHandler;
                this.customOAuth2UserService = customOAuth2UserService;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                if (!securityEnabled) {
                        http.csrf(csrf -> csrf.disable())
                                        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                                        .formLogin(form -> form.disable())
                                        .oauth2Login(oauth2 -> oauth2.disable());
                        return http.build();
                }

                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(authz -> authz
                                                .requestMatchers("/actuator/**", "/", "/login", "/error", "/webjars/**",
                                                                "/css/**", "/js/**",
                                                                "/images/**", "/privacy-policy", "/import/**",
                                                                "/dto/**")
                                                .permitAll()
                                                .requestMatchers("/quiz/**", "/my-data")
                                                .authenticated()
                                                .anyRequest().authenticated())
                                .logout(logout -> logout.logoutSuccessUrl("/").invalidateHttpSession(true)
                                                .clearAuthentication(true))
                                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(authenticationSuccessHandler)
                                                .failureUrl("/login?error=true"));

                return http.build();
        }

}
