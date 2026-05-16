package com.example.chemlearn.util;

import com.example.chemlearn.core.shared.principal.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Check if authenticated
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Chưa đăng nhập hoặc token không hợp lệ!");
    }
}
