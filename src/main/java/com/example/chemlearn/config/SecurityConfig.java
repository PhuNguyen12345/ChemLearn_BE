package com.example.chemlearn.config;

import com.example.chemlearn.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtFilter jwtFilter;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .formLogin(form -> form.disable())
//                .httpBasic(basicAuth -> basicAuth.disable())
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/auth/**","/api/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để test Postman cho dễ
            .authorizeHttpRequests(auth -> auth
                    // Cho phép truy cập mọi API bắt đầu bằng /test/ mà không cần login
                    .requestMatchers("/test/**").permitAll()
                    .anyRequest().authenticated()
            );
    return http.build();
}
}
