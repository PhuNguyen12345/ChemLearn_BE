package com.example.chemlearn.service.serviceImpl;

import com.example.chemlearn.entity.Account;
import com.example.chemlearn.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final AccountRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) {

        Account account = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole().name().replace("ROLE_", ""))
                .disabled(!account.isEnabled())
                .build();
    }
}
