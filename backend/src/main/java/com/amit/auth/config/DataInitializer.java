package com.amit.auth.config;

import com.amit.auth.entity.User;
import com.amit.auth.entity.UserType;
import com.amit.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create Administrator if it does not exist
            if (!userRepository.existsByUsername("adminuser")) {

                User admin = new User();

                admin.setUsername("adminuser");
                admin.setEmail("admin@example.com");
                admin.setPassword(
                        passwordEncoder.encode("Admin@1234")
                );
                admin.setUserType(UserType.ADMINISTRATOR);

                userRepository.save(admin);
            }

            // Create Standard User if it does not exist
            if (!userRepository.existsByUsername("standarduser")) {

                User standardUser = new User();

                standardUser.setUsername("standarduser");
                standardUser.setEmail("standard@example.com");
                standardUser.setPassword(
                        passwordEncoder.encode("Test@1234")
                );
                standardUser.setUserType(UserType.STANDARD_USER);

                userRepository.save(standardUser);
            }
        };
    }
}