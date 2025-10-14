package com.int371.eventhub.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequest;
import com.int371.eventhub.dto.RegisterOtpVerificationRequest;
import com.int371.eventhub.exception.RequestCooldownException;
import com.int371.eventhub.repository.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class OtpService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    private final Cache registrationOtpCache;
    private final Cache registrationCooldownCache;
    private final Cache loginOtpCache;
    private final Cache loginCooldownCache;

    public OtpService(CacheManager cacheManager) {
        this.registrationOtpCache = cacheManager.getCache("registrationOtp");
        this.registrationCooldownCache = cacheManager.getCache("registrationCooldown");
        this.loginOtpCache = cacheManager.getCache("loginOtp");
        this.loginCooldownCache = cacheManager.getCache("loginCooldown");
    }

    public void generateAndSendOtp(RegisterOtpRequest request) {
        String email = request.getEmail();

        if (registrationCooldownCache.get(email) != null) {
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
        OtpData storedOtpData = registrationOtpCache.get(request.getEmail(), OtpData.class);

        if (storedOtpData == null) {
            throw new IllegalArgumentException("Verification failed. No OTP was requested for this email or the OTP has expired.");
        }

        if (!storedOtpData.getOtp().equals(request.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        registrationOtpCache.evict(request.getEmail());
        return storedOtpData;
    }

    public void generateAndSendLoginOtp(String email) {
        if (loginCooldownCache.get(email) != null) {
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
        String storedOtp = loginOtpCache.get(email, String.class);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP code.");
        }
        loginOtpCache.evict(email);
    }
}
