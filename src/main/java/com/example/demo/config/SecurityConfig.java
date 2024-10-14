package com.example.demo.config;

import java.io.IOException;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.demo.dto.ErrorResponseDto;
import com.example.demo.filter.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/myAccount", ",myBalance").hasRole("ADMIN")
                .requestMatchers("/myCards", "/myLoans").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/login", "/h2-console/**","/ws").permitAll()
                .anyRequest().authenticated());

        // http.cors(null);

        http.exceptionHandling(hanlder -> hanlder
                .authenticationEntryPoint(new AuthenticationEntryPoint() {

                    @Override
                    public void commence(HttpServletRequest request,
                            HttpServletResponse response,
                            AuthenticationException authException)
                            throws IOException, ServletException {
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.setStatus(401);
                        ErrorResponseDto errorResponseDto = new ErrorResponseDto(request.getServletPath(),
                                HttpStatus.UNAUTHORIZED, authException.getMessage(), new Date());
                        ObjectMapper objectMapper = new ObjectMapper();
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
                        response.getWriter().flush();
                    }

                })
                .accessDeniedHandler(new AccessDeniedHandler() {

                    @Override
                    public void handle(HttpServletRequest request,
                            HttpServletResponse response,
                            AccessDeniedException accessDeniedException)
                            throws IOException, ServletException {
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.setStatus(403);
                        ErrorResponseDto errorResponseDto = new ErrorResponseDto(request.getServletPath(),
                                HttpStatus.UNAUTHORIZED, accessDeniedException.getMessage(), new Date());
                        ObjectMapper objectMapper = new ObjectMapper();
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponseDto));
                        response.getWriter().flush();
                    }

                }));

        http.csrf(csrf -> csrf.disable()).addFilterBefore(jwtFilter, BasicAuthenticationFilter.class);

        http.httpBasic(Customizer.withDefaults());

        // http.formLogin(null)

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers(header -> header.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
