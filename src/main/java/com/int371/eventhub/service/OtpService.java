package com.int371.eventhub.service;


import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequest;
import com.int371.eventhub.dto.RegisterOtpVerificationRequest;
import com.int371.eventhub.exception.RequestCooldownException;
import com.int371.eventhub.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;

@Service
public class OtpService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    private Cache<String, OtpData> registrationOtpCache;
    private Cache<String, Boolean> registrationCooldownCache;
    private Cache<String, String> loginOtpCache;
    private Cache<String, Boolean> loginCooldownCache;

    @PostConstruct
    public void init() {
        registrationOtpCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        registrationCooldownCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        loginOtpCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        loginCooldownCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    public void generateAndSendOtp(RegisterOtpRequest request) {
        String email = request.getEmail();

        if (registrationCooldownCache.getIfPresent(email) != null) {
            throw new RequestCooldownException("Please wait 1 minute before requesting another OTP.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Error: Email is already registered!");
        }

        String otp = new SecureRandom().ints(0, 10).limit(6).mapToObj(String::valueOf).reduce("", String::concat);

        OtpData otpData = new OtpData(otp, request.getFirstName(), request.getLastName());
        registrationOtpCache.put(email, otpData);

        try {
            emailService.sendOtpEmailHtml(email, otp);
            registrationCooldownCache.put(email, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to send OTP email, please try again later.");
        }
    }

    public OtpData verifyRegistrationOtp(RegisterOtpVerificationRequest request) {
        OtpData storedOtpData = registrationOtpCache.getIfPresent(request.getEmail());

        if (storedOtpData == null) {
            throw new IllegalArgumentException("Verification failed. No OTP was requested for this email or the OTP has expired.");
        }

        if (!storedOtpData.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        registrationOtpCache.invalidate(request.getEmail());
        return storedOtpData;
    }

    public void generateAndSendLoginOtp(String email) {
        if (loginCooldownCache.getIfPresent(email) != null) {
            throw new RequestCooldownException("Please wait 1 minute before requesting another OTP.");
        }
        if (!userRepository.existsByEmail(email)) {
            throw new UsernameNotFoundException("User with this email not found.");
        }

        String otp = new SecureRandom().ints(0, 10).limit(6).mapToObj(String::valueOf).reduce("", String::concat);
        loginOtpCache.put(email, otp);

        try {
            emailService.sendOtpEmailHtml(email, otp);
            loginCooldownCache.put(email, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to send OTP email, please try again later.");
        }
    }

    public void verifyLoginOtp(String email, String otp) {
        String storedOtp = loginOtpCache.getIfPresent(email);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP code.");
        }
        loginOtpCache.invalidate(email);
    }
}
