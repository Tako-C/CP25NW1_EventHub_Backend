package com.int371.eventhub.service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.int371.eventhub.dto.OtpData;
import com.int371.eventhub.dto.RegisterOtpRequestDto;
import com.int371.eventhub.dto.RegisterOtpVerifyRequestDto;
import com.int371.eventhub.exception.RequestCooldownException;
import com.int371.eventhub.exception.ResourceNotFoundException;
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
    private final Cache forgotPasswordOtpCache;
    private final Cache forgotPasswordCooldownCache;

    public OtpService(CacheManager cacheManager) {
        this.registrationOtpCache = cacheManager.getCache("registrationOtp");
        this.registrationCooldownCache = cacheManager.getCache("registrationCooldown");
        this.loginOtpCache = cacheManager.getCache("loginOtp");
        this.loginCooldownCache = cacheManager.getCache("loginCooldown");
        this.forgotPasswordOtpCache = cacheManager.getCache("forgotPasswordOtp");
        this.forgotPasswordCooldownCache = cacheManager.getCache("forgotPasswordCooldown");
    }

    public void generateAndSendOtp(RegisterOtpRequestDto request) {
        String email = request.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Error: Email is already registered!");
        }
        String otp = generateAndSendOtpLogic(email, registrationCooldownCache);
        OtpData otpData = new OtpData(otp, request.getFirstName(), request.getLastName(), request.getPassword());

        registrationOtpCache.put(email, otpData);
    }

        public void generateAndSendLoginOtp(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("User with this email not found.");
        }
        String otp = generateAndSendOtpLogic(email, loginCooldownCache);
        loginOtpCache.put(email, otp);
    }

    public OtpData verifyRegistrationOtp(RegisterOtpVerifyRequestDto request) {
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

    public void verifyLoginOtp(String email, String otp) {
        String storedOtp = loginOtpCache.get(email, String.class);

        if (storedOtp == null) {
            throw new IllegalArgumentException("Verification failed. No OTP was requested for this email or the OTP has expired.");
        }

        if (!storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }
        loginOtpCache.evict(email);
    }

    private String generateAndSendOtpLogic(String email, org.springframework.cache.Cache cooldownCache) {
        String timeText = "1 minute";
        if ("forgotPasswordCooldown".equals(cooldownCache.getName()) || "forgotPasswordOtp".equals(cooldownCache.getName())) {
            timeText = "5 minutes";
        }

        if (cooldownCache.putIfAbsent(email, true) != null) {
            throw new RequestCooldownException("Please wait " + timeText + " before requesting another OTP.");
        }

        String otp = new SecureRandom().ints(0, 10).limit(6).mapToObj(String::valueOf).reduce("", String::concat);

        try {
            emailService.sendOtpEmailHtml(email, otp);
            cooldownCache.put(email, true);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to send OTP email, please try again later.");
        }
        return otp;
    }
    // 1. สร้างและส่ง OTP สำหรับ Forgot Password
    public void generateAndSendForgotPasswordOtp(String email) {
        // ตรวจสอบก่อนว่ามี User นี้จริงไหม (กันคนแกล้งส่งมั่ว)
        if (!userRepository.existsByEmail(email)) {
            // อาจจะ throw exception หรือแค่ return เพื่อไม่ให้ hacker รู้ว่าอีเมลนี้ไม่มีจริง
            throw new ResourceNotFoundException("User with this email not found.");
        }
        // ใช้ logic กลางที่คุณทำไว้ (ได้ทั้ง OTP และจัดการ Cooldown 1 นาที)
        String otp = generateAndSendOtpLogic(email, forgotPasswordCooldownCache);
        // เก็บ OTP ลง cache แยกต่างหาก
        forgotPasswordOtpCache.put(email, otp);
    }

    // 2. ยืนยัน OTP
    public void verifyForgotPasswordOtp(String email, String otp) {
        String storedOtp = forgotPasswordOtpCache.get(email, String.class);
        if (storedOtp == null) {
            throw new IllegalArgumentException("Verification failed. OTP has expired or was never requested.");
        }
        if (!storedOtp.equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }
        // ยืนยันสำเร็จ ลบออกจาก cache ทันทีเพื่อป้องกันการใช้ซ้ำ (Replay Attack)
        forgotPasswordOtpCache.evict(email);
    }
}
