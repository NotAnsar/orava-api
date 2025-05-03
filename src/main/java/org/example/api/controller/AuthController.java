package org.example.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.UserDTO;
import org.example.api.model.UserRole;
import org.example.api.payload.request.auth.LoginRequest;
import org.example.api.payload.request.auth.RegisterRequest;
import org.example.api.payload.response.AuthResponse;
import org.example.api.repository.UserRepository;
import org.example.api.security.jwt.JwtUtils;
import org.example.api.security.services.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // First check if user exists
        if (!userRepository.existsByEmail(loginRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Error: User not found",null,null,false));
        }

        try {
            // User exists, now attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // If we reach here, authentication was successful
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new AuthResponse(
                    "User logged in successfully!",
                    new AuthResponse.UserData(
                            userDetails.getId(),
                            userDetails.getFirstName(),
                            userDetails.getLastName(),
                            userDetails.getUsername(),
                            userDetails.getRole(),
                            userDetails.getCreatedAt()
                    ),
                    jwt
            ));
        } catch (BadCredentialsException e) {
            // We know the user exists, so this is definitely an invalid password
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Error: Invalid password",null,null,false));

        } catch (AuthenticationException e) {
            // Other authentication failures
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse("Error: Authentication failed",null,null,false));

        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        System.out.println(registerRequest.getEmail());
        System.out.println(registerRequest);
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse("Error: Email is already in use!",null,null,false));

        }

        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(registerRequest.getFirstName());
        userDTO.setLastName(registerRequest.getLastName());
        userDTO.setEmail(registerRequest.getEmail());
        userDTO.setPassword(registerRequest.getPassword());
        userDTO.setCreatedAt(ZonedDateTime.now());
        userDTO.setRole(UserRole.USER);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new AuthResponse(
                "User registered successfully!",
                new AuthResponse.UserData(
                        userDetails.getId(),
                        userDetails.getFirstName(),
                        userDetails.getLastName(),
                        userDetails.getUsername(),
                        userDetails.getRole(),
                        userDetails.getCreatedAt()
                ),
                jwt
        ));
    }
}