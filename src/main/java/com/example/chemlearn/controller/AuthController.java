package com.example.chemlearn.controller;

import com.example.chemlearn.dtos.auth.AuthResponseDTO;
import com.example.chemlearn.dtos.auth.LoginRequestDTO;
import com.example.chemlearn.dtos.auth.RegisterRequestDTO;
import com.example.chemlearn.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO dto) {
        System.out.println("Login controller hit!!!");
        return ResponseEntity.ok(authService.login(dto));
    }
}
