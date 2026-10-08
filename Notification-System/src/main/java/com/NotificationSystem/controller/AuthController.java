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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest authenticationRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect username or password");
        }

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(
                        authenticationRequest.getUsername()
                );

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        String jwt = jwtUtil.generateToken(user);

        ResponseCookie cookie = ResponseCookie
                .from("access_token", jwt)
                .httpOnly(true)
                .secure(false) // false for local HTTP development
                .sameSite("Lax")
                .path("/")
                .maxAge(60 * 60 * 10)
                .build();

        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookie.toString())
                .body(
                        new AuthenticationResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getName()

                        )
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        ResponseCookie cookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(false) // true in production
                .sameSite("Lax")
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

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User savedUser = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        "User registered successfully as "
                                + savedUser.getRole().name()
                );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(
                new AuthenticationResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getName()

                )
        );
    }
}