package com.amit.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amit.auth.dto.AuthResponse;
import com.amit.auth.dto.SignupRequest;
import com.amit.auth.entity.User;
import com.amit.auth.repository.UserRepository;
import com.amit.auth.security.JwtService;
import com.amit.auth.exception.InvalidCredentialsException;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private final TokenBlacklistService tokenBlacklistService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
			TokenBlacklistService tokenBlacklistService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	public User signup(SignupRequest request) {

		if (userRepository.existsByUsername(request.getUsername())) {
			throw new RuntimeException("Username already exists");
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already exists");
		}

		if (!isValidPassword(request.getPassword())) {
			throw new RuntimeException(
					"Password must contain at least 1 uppercase, 1 lowercase, 1 number and 1 special character");
		}

		User user = new User();

		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());

		// Never save the plaintext password
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		user.setUserType(request.getUserType());

		return userRepository.save(user);
	}

	private boolean isValidPassword(String password) {

		String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,12}$";

		return password != null && password.matches(passwordPattern);
	}

	public AuthResponse login(String username, String password) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new InvalidCredentialsException("Login Failed"));
		

		if (!passwordEncoder.matches(password, user.getPassword())) {
			throw new InvalidCredentialsException("Login Failed");
		}

		return jwtService.generateAuthResponse(user.getUsername(), user.getUserType().name());
	}

	public String logout(String token) {

		if (!jwtService.isTokenValid(token)) {
			throw new RuntimeException("Invalid token");
		}

		String username = jwtService.extractUsername(token);

		tokenBlacklistService.blacklistToken(token);

		return username;
	}
}