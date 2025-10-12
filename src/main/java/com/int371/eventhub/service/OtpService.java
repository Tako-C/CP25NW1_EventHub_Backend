package com.int371.eventhub.service;


import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.OtpVerificationRequest;
import com.int371.eventhub.entity.User;
import com.int371.eventhub.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class OtpService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final ConcurrentHashMap<String, String> otpCache = new ConcurrentHashMap<>();

    public void generateAndSendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Error: Email is already registered!");
        }

        String otp = new SecureRandom().ints(0, 10).limit(6).mapToObj(String::valueOf).reduce("", String::concat);
        otpCache.put(email, otp);

        try {
            emailService.sendOtpEmailHtml(email, otp);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to send OTP email, please try again later.");
        }
    }

    public User verifyOtpAndRegister(OtpVerificationRequest request) {
        String storedOtp = otpCache.get(request.getEmail());

        if (storedOtp == null) {
            throw new IllegalArgumentException("OTP has expired or never requested.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setJobId(1);
        user.setTotalPoint(0);
        user.setStatusId(1);
        user.setRoleId(1);

        String randomPassword = RandomStringUtils.randomAlphanumeric(10);
        user.setPassword(passwordEncoder.encode(randomPassword));

        otpCache.remove(request.getEmail());
        return userRepository.save(user);
    }
}
