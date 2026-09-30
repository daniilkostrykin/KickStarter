package org.example.kickstarterrest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Profile("session-auth")
@Configuration
@EnableMethodSecurity
public class SessionSecurityConfig {

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain sessionSecurityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
                .formLogin(form -> form.defaultSuccessUrl("/api/projects", true))
                .logout(logout -> logout.deleteCookies("JSESSIONID", "XSRF-TOKEN"))
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(1));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                // 1. Спонсор: только просмотр
                User.withUsername("backer")
                        .password(encoder.encode("backer"))
                        .roles("BACKER")
                        .build(),

                // 2. Автор: просмотр, создание и редактирование
                User.withUsername("creator")
                        .password(encoder.encode("creator"))
                        .roles("BACKER", "CREATOR")
                        .build(),

                // 3. Администратор: полный доступ, включая удаление
                User.withUsername("admin")
                        .password(encoder.encode("admin"))
                        .roles("BACKER", "CREATOR", "ADMIN")
                        .build()
        );
    }
}