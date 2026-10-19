package com.NotificationSystem.security;

import com.NotificationSystem.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final PasswordEncoder passwordEncoder;
//    private final SecurityContextRepository securityContextRepository;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http

                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {

                    CorsConfiguration configuration =
                            new CorsConfiguration();

                    configuration.setAllowedOrigins(
                            Arrays.asList(
                                    "http://localhost:3000"
                            )
                    );

                    configuration.setAllowedMethods(
                            Arrays.asList(
                                    "GET",
                                    "POST",
                                    "PUT",
                                    "DELETE",
                                    "OPTIONS"
                            )
                    );

                    configuration.setAllowedHeaders(
                            Arrays.asList(
                                    "Content-Type",
                                    "Authorization"
                            )
                    );

                    configuration.setAllowCredentials(true);

                    return configuration;
                }))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
//                .securityContext(context ->
//                        context.securityContextRepository(
//                                securityContextRepository
//                        )
//                )

                .authorizeHttpRequests(auth -> auth

                                // Public endpoints (register is no longer public)
                                .requestMatchers(
                                        "/api/auth/authenticate",
                                        "/test/**",
                                        "/webhook/**",
                                        "/error"
                                ).permitAll()

                                // Only admins can create new accounts
                                .requestMatchers("/api/auth/register").hasRole("ADMIN")

                                // Admin endpoints
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/reports/**").hasRole("ADMIN")

                                // Staff endpoints
                                .requestMatchers("/staff/**").hasRole("STAFF")

                                // Zones: admin and staff can view, only admins can change
                                .requestMatchers(HttpMethod.GET, "/zones/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/zones/**").hasRole("ADMIN")

                                // Residents: staff can view, add and edit, only admins can delete
                                .requestMatchers(HttpMethod.DELETE, "/residents/**").hasRole("ADMIN")


                                .requestMatchers("/api/sms/**").hasRole("ADMIN")
                                .requestMatchers("/api/whatsapp/**").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/trucks/**").hasAnyRole("ADMIN", "STAFF")
                                .requestMatchers("/trucks/**").hasRole("ADMIN")

                                .anyRequest().authenticated()
                        )

                .addFilterBefore(
                        jwtRequestFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }


    @Bean
    public AuthenticationManager authManager(
            HttpSecurity http
    ) throws Exception {

        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(
                        AuthenticationManagerBuilder.class
                );

        authenticationManagerBuilder.authenticationProvider(
                daoAuthenticationProvider()
        );

        return authenticationManagerBuilder.build();
    }


    @Bean
    public AuthenticationProvider daoAuthenticationProvider() {

        DaoAuthenticationProvider daoAuthenticationProvider =
                new DaoAuthenticationProvider(userDetailsService);

        daoAuthenticationProvider.setPasswordEncoder(
                passwordEncoder
        );

        return daoAuthenticationProvider;
    }
}
