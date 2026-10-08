package com.amit.auth.service;

import com.amit.auth.dto.OtpResponse;
import com.amit.auth.dto.SignupOtpRequest;
import com.amit.auth.dto.VerifyOtpRequest;
import com.amit.auth.entity.PendingSignup;
import com.amit.auth.repository.PendingSignupRepository;
import com.amit.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final PendingSignupRepository pendingSignupRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.test-mode:false}")
    private boolean testMode;

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    public OtpService(
            PendingSignupRepository pendingSignupRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.pendingSignupRepository = pendingSignupRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public OtpResponse generateOtp(SignupOtpRequest request) {

        /*
         * User should not already exist.
         */
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already exists"
            );
        }

        /*
         * Remove previous pending registration
         * for this email/username.
         */
        pendingSignupRepository
                .findByEmail(request.getEmail())
                .ifPresent(pendingSignup ->
                        pendingSignupRepository.delete(pendingSignup)
                );

        pendingSignupRepository
                .findByUsername(request.getUsername())
                .ifPresent(pendingSignup ->
                        pendingSignupRepository.delete(pendingSignup)
                );

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        PendingSignup pendingSignup =
                new PendingSignup();

        pendingSignup.setUsername(
                request.getUsername()
        );

        pendingSignup.setEmail(
                request.getEmail()
        );

        /*
         * Store BCrypt hash, never plaintext password.
         */
        pendingSignup.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        pendingSignup.setUserType(
                request.getUserType()
        );

        pendingSignup.setOtp(otp);

        pendingSignup.setOtpExpiresAt(
                LocalDateTime.now()
                        .plusMinutes(expirationMinutes)
        );

        pendingSignupRepository.save(pendingSignup);

        /*
         * In real production mode this is where
         * email/SMS delivery would happen.
         */
        String responseOtp = testMode ? otp : null;

        return new OtpResponse(
                "OTP generated successfully",
                responseOtp
        );
    }

    public String verifyOtp(VerifyOtpRequest request) {

        PendingSignup pendingSignup =
                pendingSignupRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No pending registration found"
                                )
                        );

        if (LocalDateTime.now()
                .isAfter(pendingSignup.getOtpExpiresAt())) {

            pendingSignupRepository.delete(pendingSignup);

            throw new RuntimeException(
                    "OTP has expired"
            );
        }

        if (!pendingSignup.getOtp()
                .equals(request.getOtp())) {

            throw new RuntimeException(
                    "Invalid OTP"
            );
        }

        /*
         * Double-check that the user wasn't created
         * while OTP verification was pending.
         */
        if (userRepository.existsByUsername(
                pendingSignup.getUsername())) {

            pendingSignupRepository.delete(pendingSignup);

            throw new RuntimeException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(
                pendingSignup.getEmail())) {

            pendingSignupRepository.delete(pendingSignup);

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        /*
         * Create the actual user.
         */
        com.amit.auth.entity.User user =
                new com.amit.auth.entity.User();

        user.setUsername(
                pendingSignup.getUsername()
        );

        user.setEmail(
                pendingSignup.getEmail()
        );

        user.setPassword(
                pendingSignup.getPassword()
        );

        user.setUserType(
                pendingSignup.getUserType()
        );

        userRepository.save(user);

        /*
         * OTP can no longer be reused.
         */
        pendingSignupRepository.delete(
                pendingSignup
        );

        return "Registration successful";
    }
}

