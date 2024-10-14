package com.example.demo.controller;

import java.util.Date;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.model.Customer;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.service.JwtService;

import io.jsonwebtoken.Jwts;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    @GetMapping("/myAccount")
    public ResponseEntity<Object> getAccount() {
        return ResponseEntity.ok().body("This is my Account details");
    }

    @GetMapping("/myLoans")
    public ResponseEntity<Object> getLoan() {
        return ResponseEntity.ok().body("Here is my loans detailsss.");
    }

    @GetMapping("/myCards")
    public ResponseEntity<Object> getLoans() {
        return ResponseEntity.ok().body("Here is CARDS details");
    }

    @PostMapping("/contact")
    public ResponseEntity<String> postContact() {
        return ResponseEntity.ok().body("Contacts saved successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<String> postRegistrationDetails(@RequestBody Customer customer) {
        Optional<Customer> customerOptional = customerRepository.findByUsername(customer.getUsername());
        if (customerOptional.isPresent()) {
            System.out.println("Customer already present in the DB................" + customerOptional.get());
            throw new IllegalArgumentException("Customer already present in the DB................");
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customerRepository.save(customer);
        return ResponseEntity.ok().body("Registration details saved successfully");
    }

    @PostMapping("/login") // Dont use http.formLogin(Customizer.withDefaults()); inside security config if
                           // you want to use "/login" as an end point in controller, becasue "/login" is
                           // an inbuild GET endpoint inside spring incase of form login.
    public ResponseEntity<LoginResponseDto> generateJwtToken(@RequestBody LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.username();
        String password = loginRequestDto.password();
        Authentication authenticationResponse = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        if (authenticationResponse != null && authenticationResponse.isAuthenticated()) {
            String jwt = jwtService.generateJwtToken(username);
            return new ResponseEntity<LoginResponseDto>(new LoginResponseDto("201", jwt), HttpStatus.CREATED);
        } else {
            throw new BadCredentialsException("Wrong password or username");
        }
    }

}
