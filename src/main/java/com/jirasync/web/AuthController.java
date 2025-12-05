package com.jirasync.web;

import com.jirasync.domain.User;
import com.jirasync.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User u = users.findByUsername(username);
        if (u == null) return ResponseEntity.status(401).build();
        if (!encoder.matches(password, u.getPassword())) return ResponseEntity.status(401).build();
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", "dev-token");
        resp.put("userId", u.getId());
        resp.put("username", u.getUsername());
        return ResponseEntity.ok(resp);
    }
}
