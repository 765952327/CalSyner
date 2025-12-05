package com.calsync.config;

import com.calsync.domain.User;
import com.calsync.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseInitRunner {
    @Bean
    CommandLineRunner initAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            User u = users.findByUsername("admin");
            if (u == null) {
                User n = new User();
                n.setUsername("admin");
                n.setPassword(encoder.encode("admin"));
                n.setEmail("admin@jirasync.com");
                n.setRole("ADMIN");
                users.save(n);
            }
        };
    }
}
