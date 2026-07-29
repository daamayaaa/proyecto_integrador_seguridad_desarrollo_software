package com.example.securedemo.controller;

import com.example.securedemo.dto.LoginRequest;
import com.example.securedemo.dto.UserRegistrationRequest;
import com.example.securedemo.model.User;
import com.example.securedemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserRegistrationRequest request) {
        User created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", created.getId(), "username", created.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.authenticate(request);
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "status", "AUTHENTICATED"));
    }

    // Recurso protegido: requiere autenticacion (ver SecurityConfig) y aplica
    // verificacion de propiedad del recurso para evitar IDOR (Broken Object Level Authorization).
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable Long id, Authentication authentication) {
        User user = userService.getOwnProfile(id, authentication.getName());
        return ResponseEntity.ok(Map.of("id", user.getId(), "username", user.getUsername(), "email", user.getEmail()));
    }
}
