package com.archai.auth.controller;

import com.archai.auth.config.JwtService;
import com.archai.auth.dto.AuthRequest;
import com.archai.auth.dto.AuthResponse;
import com.archai.auth.dto.UserProfileResponse;
import com.archai.auth.entity.Role;
import com.archai.auth.entity.User;
import com.archai.auth.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import io.jsonwebtoken.JwtException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Full name is required");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.USER);
        userRepository.save(user);

        AuthResponse response = new AuthResponse();
        response.setToken(jwtService.generateToken(String.valueOf(user.getId()), user.getEmail(), List.of(user.getRole().name())));
        response.setUserId(String.valueOf(user.getId()));
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).build();
        }

        User user = userOpt.get();
        AuthResponse response = new AuthResponse();
        response.setToken(jwtService.generateToken(String.valueOf(user.getId()), user.getEmail(), List.of(user.getRole().name())));
        response.setUserId(String.valueOf(user.getId()));
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
        @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            long userId = Long.parseLong(jwtService.validateToken(authorization.substring(7)).getSubject());
            return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(UserProfileResponse.from(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (JwtException | IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
