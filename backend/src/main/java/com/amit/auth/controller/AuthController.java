package com.amit.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amit.auth.dto.AuthResponse;
import com.amit.auth.dto.LoginRequest;
import com.amit.auth.dto.OtpResponse;
import com.amit.auth.dto.SignupOtpRequest;
import com.amit.auth.dto.SignupRequest;
import com.amit.auth.dto.VerifyOtpRequest;
import com.amit.auth.service.AuthService;
import com.amit.auth.service.CountryRestrictionService;
import com.amit.auth.service.OtpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	private final OtpService otpService;
	
	private final CountryRestrictionService countryRestrictionService;

	public AuthController(AuthService authService, OtpService otpService, CountryRestrictionService countryRestrictionService) {
		this.authService = authService;
		this.otpService = otpService;
		this.countryRestrictionService = countryRestrictionService;
	}

	@PostMapping("/signup")
	public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request, HttpServletRequest httpRequest) {

		authService.signup(request, httpRequest);

		return ResponseEntity.ok("User registered successfully");
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

		AuthResponse response = authService.login(request.getUsername(), request.getPassword());

		return ResponseEntity.ok(response);
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.badRequest().body("Authorization token is required");
		}

		String token = authHeader.substring(7);

		String username = authService.logout(token);

		return ResponseEntity.ok(username + " Logged Out");
	}
	
	
	@PostMapping("/signup/request-otp")
	public ResponseEntity<OtpResponse> requestSignupOtp(
	        @Valid @RequestBody SignupOtpRequest request,
	        HttpServletRequest httpRequest) {

	    /*
	     * Country restriction is checked BEFORE OTP generation.
	     */
	    if (!countryRestrictionService
	            .isSignupAllowed(httpRequest)) {

	        throw new RuntimeException(
	                "Signup is not allowed from your country"
	        );
	    }

	    OtpResponse response =
	            otpService.generateOtp(request);

	    return ResponseEntity.ok(response);
	}


	@PostMapping("/signup/verify-otp")
	public ResponseEntity<String> verifySignupOtp(
	        @Valid @RequestBody VerifyOtpRequest request) {

	    String response =
	            otpService.verifyOtp(request);

	    return ResponseEntity.ok(response);
	}
	

}