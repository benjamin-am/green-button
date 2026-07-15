package com.greenbutton.backend.auth;

import com.greenbutton.backend.user.User;
import com.greenbutton.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    record Credentials(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/register")
    ResponseEntity<Map<String, String>> register(@Valid @RequestBody Credentials credentials) {
        if (users.existsByUsername(credentials.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = new User(credentials.username(), passwordEncoder.encode(credentials.password()));
        users.save(user);
        return ResponseEntity.ok(Map.of("token", jwtService.generate(user.getUsername())));
    }

    @PostMapping("/login")
    ResponseEntity<Map<String, String>> login(@Valid @RequestBody Credentials credentials) {
        User user = users.findByUsername(credentials.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(credentials.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(Map.of("token", jwtService.generate(user.getUsername())));
    }
}
