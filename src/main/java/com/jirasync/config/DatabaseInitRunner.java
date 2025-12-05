package com.jirasync.config;

import com.jirasync.domain.User;
import com.jirasync.repository.UserRepository;
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
