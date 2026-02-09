package com.jashwanth_projects.organize_it_backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Keep /tasks public for now; everything else requires auth.
                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing POST/PUT from tools like Postman
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/tasks/**").permitAll() // ✅ Allow public access to /tasks
                        .anyRequest().authenticated())
                .httpBasic(withDefaults()); // or .formLogin().disable() if you want no login UI
        return http.build();
    }
}
