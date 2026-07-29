package com.example.securedemo.service;

import com.example.securedemo.dto.LoginRequest;
import com.example.securedemo.dto.UserRegistrationRequest;
import com.example.securedemo.exception.BusinessException;
import com.example.securedemo.model.User;
import com.example.securedemo.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("El usuario ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El correo ya esta registrado");
        }
        String hash = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), request.getEmail(), hash);
        return userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                // Mensaje generico: no revela si fallo el usuario o la contrasena
                .orElseThrow(() -> new BusinessException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Credenciales invalidas");
        }
        return user;
    }

    /**
     * Control de autorizacion a nivel de objeto (evita IDOR / Broken Object Level
     * Authorization - OWASP API1): un usuario solo puede consultar su propio perfil.
     */
    public User getOwnProfile(Long requestedId, String authenticatedUsername) {
        User user = userRepository.findById(requestedId)
                .orElseThrow(() -> new BusinessException("Recurso no encontrado"));

        if (!user.getUsername().equals(authenticatedUsername)) {
            throw new AccessDeniedException("No autorizado para consultar este recurso");
        }
        return user;
    }
}
