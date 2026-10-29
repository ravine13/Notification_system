package com.NotificationSystem.controller;

import com.NotificationSystem.entities.User;
import com.NotificationSystem.repositories.UserRepository;
import com.NotificationSystem.security.JwtUtil;
import com.NotificationSystem.service.CustomUserDetailsService;
import model.AuthenticationRequest;
import model.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public record ChangeEmailRequest(String newEmail, String currentPassword) {}



    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private ResponseCookie buildAuthCookie(String jwt) {
        return ResponseCookie
                .from("access_token", jwt)
                .httpOnly(true)
                .secure(cookieSecure) // false for local HTTP development, true in production
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(60 * 60 * 10)
                .build();
    }

    private static boolean isNotAuthenticated(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    private static AuthenticationResponse toResponse(User user) {
        return new AuthenticationResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getName()
        );
    }



    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest authenticationRequest) {

        String username = normalizeEmail(authenticationRequest.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            authenticationRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwt = jwtUtil.generateToken(user);

        return ResponseEntity
                .ok()
                .header("Set-Cookie", buildAuthCookie(jwt).toString())
                .body(toResponse(user));
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        ResponseCookie cookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(cookieSecure) // true in production
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookie.toString())
                .body("Logged out successfully");
    }



    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {

        String email = normalizeEmail(user.getEmail());
        if (email.isEmpty() || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Enter a valid email address");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        user.setEmail(email);
        user.setRole(User.Role.STAFF);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully as " + savedUser.getRole().name());
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (isNotAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(toResponse(user));
    }



    @PutMapping("/me/email")
    public ResponseEntity<?> changeEmail(
            Authentication authentication,
            @RequestBody ChangeEmailRequest req) {

        if (isNotAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (req.currentPassword() == null
                || !passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Current password is incorrect");
        }

        String newEmail = normalizeEmail(req.newEmail());
        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            return ResponseEntity.badRequest().body("Enter a valid email address");
        }

        var existing = userRepository.findByEmail(newEmail);
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("That email is already in use");
        }

        user.setEmail(newEmail);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(user);

        return ResponseEntity
                .ok()
                .header("Set-Cookie", buildAuthCookie(jwt).toString())
                .body(toResponse(user));
    }
}