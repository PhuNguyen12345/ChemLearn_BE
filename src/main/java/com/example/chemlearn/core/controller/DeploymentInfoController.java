package com.example.chemlearn.core.controller;

import com.example.chemlearn.config.RequiredProductionDataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/public", "/api/auth"})
@RequiredArgsConstructor
public class DeploymentInfoController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/deployment-info")
    public Map<String, Object> deploymentInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("buildMarker", RequiredProductionDataSeeder.buildMarker());
        info.put("latestFlyway", latestFlyway());
        info.put("admin", adminStatus());
        info.put("premadeLabCount", countPremadeLabs());
        info.put("premadeLabs", premadeLabs());
        return info;
    }

    private Map<String, Object> latestFlyway() {
        try {
            return jdbcTemplate.query("""
                            SELECT version, description, success
                            FROM flyway_schema_history
                            ORDER BY installed_rank DESC
                            LIMIT 1
                            """,
                    rs -> {
                        if (!rs.next()) {
                            return Map.of("status", "empty");
                        }
                        Map<String, Object> result = new LinkedHashMap<>();
                        result.put("version", rs.getString("version"));
                        result.put("description", rs.getString("description"));
                        result.put("success", rs.getBoolean("success"));
                        return result;
                    });
        } catch (Exception ex) {
            return Map.of("status", "unavailable", "message", ex.getMessage());
        }
    }

    private Map<String, Object> adminStatus() {
        return jdbcTemplate.query("""
                        SELECT username, role, is_active, failed_login_attempts, lockout_until
                        FROM users
                        WHERE username = 'duckhisuu'
                        """,
                rs -> {
                    if (!rs.next()) {
                        return Map.of("exists", false);
                    }
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("exists", true);
                    result.put("username", rs.getString("username"));
                    result.put("role", rs.getString("role"));
                    result.put("isActive", rs.getBoolean("is_active"));
                    result.put("failedLoginAttempts", rs.getObject("failed_login_attempts"));
                    result.put("lockoutUntil", rs.getObject("lockout_until"));
                    return result;
                });
    }

    private Integer countPremadeLabs() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM lab WHERE type = 'PREMADE'",
                Integer.class
        );
    }

    private List<Map<String, Object>> premadeLabs() {
        return jdbcTemplate.query("""
                        SELECT title, category, difficulty
                        FROM lab
                        WHERE type = 'PREMADE'
                        ORDER BY title
                        """,
                (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("title", rs.getString("title"));
                    row.put("category", rs.getString("category"));
                    row.put("difficulty", rs.getString("difficulty"));
                    return row;
                });
    }
}
