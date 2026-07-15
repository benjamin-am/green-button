package com.greenbutton.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository users;

    @GetMapping("/me")
    Map<String, String> me(Principal principal) {
        User user = users.findByUsername(principal.getName()).orElseThrow();
        return Map.of("username", user.getUsername(), "avatar", user.getAvatar());
    }
}
