package com.jashwanth_projects.organize_it_backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF for testing POST/PUT from tools like Postman
            .authorizeHttpRequests()
                .requestMatchers("/tasks/**").permitAll() // ✅ Allow public access to /tasks
                .anyRequest().authenticated() // everything else requires login (optional)
            .and()
            .httpBasic(); // or .formLogin().disable() if you want no login UI
        return http.build();
    }
}
