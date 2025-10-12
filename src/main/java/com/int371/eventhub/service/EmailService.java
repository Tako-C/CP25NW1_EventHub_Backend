package com.int371.eventhub.service;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOtpEmailHtml(String email, String otp) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = buildOtpEmailContent(otp);

        helper.setFrom("noreply@eventhub.app.co.th", "EventHub Team");
        helper.setTo(email);
        helper.setSubject("Your EventHub Verification Code");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildOtpEmailContent(String otp) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body {font-family: Arial, sans-serif; margin: 20px; color: #333;}"
                + ".container {border: 1px solid #ddd; padding: 20px; max-width: 600px; margin: auto;}"
                + ".header {font-size: 24px; color: #4CAF50; margin-bottom: 20px;}"
                + ".otp-code {font-size: 36px; font-weight: bold; color: #333; letter-spacing: 5px; margin: 20px 0; padding: 10px; background-color: #f2f2f2; text-align: center;}"
                + ".footer {font-size: 12px; color: #777; margin-top: 20px;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>EventHub Verification Code</div>"
                + "<p>Thank you for registering. Please use the following One-Time Password (OTP) to complete your registration:</p>"
                + "<div class='otp-code'>" + otp + "</div>"
                + "<p>This code is valid for 5 minutes.</p>"
                + "<div class='footer'>If you did not request this code, please ignore this email.</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}