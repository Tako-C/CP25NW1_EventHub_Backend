package com.int371.eventhub.service;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.LoginOtpVerificationRequest;
import com.int371.eventhub.dto.LoginRequest;
import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpVerificationRequest;
import com.int371.eventhub.dto.RegisterRequest;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.entity.UserRole;
import com.int371.eventhub.repository.UserRepository;
import com.int371.eventhub.repository.UserRoleRepository;

import jakarta.mail.MessagingException;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private EmailService emailService;

    private static final Integer DEFAULT_ROLE_ID = 3;
    private static final Integer DEFAULT_JOB_ID = 1;
    private static final Integer DEFAULT_STATUS_ID = 1;
    private static final Integer DEFAULT_TOTAL_POINT = 0;
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+|~-=\\`{}[]:\";'<>?,./";
    

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        return createUser(request.getFirstName(), request.getLastName(), request.getEmail(), encodedPassword);
    }

    public User registerWithOtp(RegisterOtpVerificationRequest request) {
        OtpData otpData = otpService.verifyRegistrationOtp(request);
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Error: Email has just been registered!");
        }

        String randomPassword = RandomStringUtils.random(16, PASSWORD_CHARACTERS);
        String encodedPassword = passwordEncoder.encode(randomPassword);

        User savedUser = createUser(otpData.getFirstName(), otpData.getLastName(), email, encodedPassword);

        try {
            emailService.sendWelcomePasswordEmail(savedUser.getEmail(), savedUser.getFirstName(), randomPassword);
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Failed to send welcome email to " + savedUser.getEmail());
        }

        return savedUser;
    }

    private User createUser(String firstName, String lastName, String email, String encodedPassword) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        UserRole defaultRole = userRoleRepository.findById(DEFAULT_ROLE_ID)
                .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
        user.setRole(defaultRole);
        
        user.setJobId(DEFAULT_JOB_ID);
        user.setTotalPoint(DEFAULT_TOTAL_POINT);
        user.setStatusId(DEFAULT_STATUS_ID);

        return userRepository.save(user);
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return jwtService.generateToken(user);
    }

    public String loginWithOtp(LoginOtpVerificationRequest request) {
        otpService.verifyLoginOtp(request.getEmail(), request.getOtp());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return jwtService.generateToken(user);
    }
}