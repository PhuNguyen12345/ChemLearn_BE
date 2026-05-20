package com.example.chemlearn.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    @Value("${auth.rate-limit.ip.max-requests:30}")
    private int maxRequests;

    @Value("${auth.rate-limit.ip.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, List<Long>> ipRequestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate limit specifically to auth endpoints (e.g. login, register, onboarding requests)
        if (path.startsWith("/api/auth/") || path.startsWith("api/auth/")) {
            String ip = getClientIp(request);
            long now = System.currentTimeMillis();
            long windowMillis = windowSeconds * 1000L;

            List<Long> timestamps = ipRequestTimestamps.computeIfAbsent(ip, k -> Collections.synchronizedList(new ArrayList<>()));

            synchronized (timestamps) {
                // Remove outdated timestamps
                timestamps.removeIf(timestamp -> now - timestamp > windowMillis);

                if (timestamps.size() >= maxRequests) {
                    response.setStatus(429); // Too Many Requests
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"code\":\"TOO_MANY_REQUESTS\",\"message\":\"Too many requests. Please try again later.\"}");
                    return;
                }

                timestamps.add(now);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.trim().isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
