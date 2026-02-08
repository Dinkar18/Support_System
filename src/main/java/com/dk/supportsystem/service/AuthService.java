package com.dk.supportsystem.service;

import com.dk.supportsystem.dto.request.LoginRequest;
import com.dk.supportsystem.dto.request.SignupRequest;
import com.dk.supportsystem.dto.response.AuthResponse;
import com.dk.supportsystem.dto.response.UserResponse;
import com.dk.supportsystem.entity.Organization;
import com.dk.supportsystem.entity.User;
import com.dk.supportsystem.enums.UserRole;
import com.dk.supportsystem.exception.BadRequestException;
import com.dk.supportsystem.repository.OrganizationRepository;
import com.dk.supportsystem.repository.UserRepository;
import com.dk.supportsystem.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        Organization organization = organizationRepository
                .findByName(request.getOrganizationName())
                .orElseGet(() -> organizationRepository.save(
                        Organization.builder()
                                .name(request.getOrganizationName())
                                .build()
                ));
        
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.ADMIN)
                .organization(organization)
                .isActive(true)
                .build();
        
        user = userRepository.save(user);
        
        String accessToken = tokenProvider.generateAccessToken(
                user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }
        
        if (!user.getIsActive()) {
            throw new BadRequestException("Account is inactive");
        }
        
        String accessToken = tokenProvider.generateAccessToken(
                user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapToUserResponse(user))
                .build();
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .organizationName(user.getOrganization().getName())
                .build();
    }
}
