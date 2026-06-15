package com.example.chemlearn.config;

import com.example.chemlearn.core.entity.User;
import com.example.chemlearn.core.enums.AuthProvider;
import com.example.chemlearn.core.enums.UserRole;
import com.example.chemlearn.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

import static com.example.chemlearn.util.PasswordUtil.hash;

@Configuration
@RequiredArgsConstructor
public class AdminAccountSeeder {

    private final UserRepository userRepository;

    @Value("${app.seed-admin.enabled:true}")
    private boolean seedAdminEnabled;

    @Value("${app.seed-admin.username:duckhisuu}")
    private String adminUsername;

    @Value("${app.seed-admin.password:Hieubeep1407@}")
    private String adminPassword;

    @Value("${app.seed-admin.email:duckhisuu@chemlearn.local}")
    private String adminEmail;

    @Value("${app.seed-admin.full-name:Duck Hisuu}")
    private String adminFullName;

    @Bean
    public CommandLineRunner seedDefaultAdminAccount() {
        return args -> {
            if (!seedAdminEnabled) {
                return;
            }

            userRepository.findByUsername(adminUsername)
                    .map(existing -> {
                        existing.setRole(UserRole.ROLE_ADMIN);
                        existing.setIsActive(true);
                        existing.setAuthProvider(AuthProvider.LOCAL);
                        existing.setUpdatedAt(Instant.now());
                        return userRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        User admin = new User();
                        admin.setUsername(adminUsername);
                        admin.setFullName(adminFullName);
                        admin.setEmail(adminEmail);
                        admin.setPassword(hash(adminPassword));
                        admin.setRole(UserRole.ROLE_ADMIN);
                        admin.setIsActive(true);
                        admin.setAuthProvider(AuthProvider.LOCAL);
                        admin.setFailedLoginAttempts(0);
                        admin.setCreatedAt(Instant.now());
                        return userRepository.save(admin);
                    });
        };
    }
}
