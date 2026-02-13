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

        helper.setFrom("noreply@eventhub.com", "EventHub Team");
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
                + ".header {font-size: 24px; color: #3569adff; margin-bottom: 20px;}"
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

    @Async
    public void sendWelcomeEmail(String to, String firstName, String password)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = buildWelcomeEmailContent(firstName, password);

        helper.setFrom("noreply@eventhub.com", "EventHub Team");
        helper.setTo(to);
        helper.setSubject("Welcome to EventHub!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildWelcomeEmailContent(String firstName, String password) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body {font-family: Arial, sans-serif; color: #333;}"
                + ".container {border: 1px solid #ddd; padding: 20px; max-width: 600px; margin: auto;}"
                + ".header {font-size: 24px; color: #007bff; margin-bottom: 20px;}"
                + ".password-box {background-color: #f2f2f2; border: 1px dashed #ccc; padding: 15px; font-size: 20px; font-family: 'Courier New', monospace; text-align: center; margin: 20px 0;}"
                + ".warning {color: #d9534f; font-weight: bold;}"
                + ".footer {font-size: 12px; color: #777; margin-top: 20px;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>Welcome to EventHub, " + firstName + "!</div>"
                + "<p>Your registration is complete. You can now log in using your email and this automatically generated password:</p>"
                + "<div class='password-box'>" + password + "</div>"
                + "<p class='warning'>For your security, we strongly recommend changing this password after your first login.</p>"
                + "<div class='footer'>Thank you for joining EventHub!</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = buildSimpleWelcomeEmailContent(firstName);

        helper.setFrom("noreply@eventhub.com", "EventHub Team");
        helper.setTo(to);
        helper.setSubject("Welcome to EventHub!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildSimpleWelcomeEmailContent(String firstName) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body {font-family: Arial, sans-serif; color: #333;}"
                + ".container {border: 1px solid #ddd; padding: 20px; max-width: 600px; margin: auto;}"
                + ".header {font-size: 24px; color: #007bff; margin-bottom: 20px;}"
                + ".footer {font-size: 12px; color: #777; margin-top: 20px;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>Welcome to EventHub, " + firstName + "!</div>"
                + "<p>Your registration is complete. You can now log in using your email and password.</p>"
                + "<div class='footer'>Thank you for joining EventHub!</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    @Async
    public void sendPostSurveyEmail(String to, String userName, String eventName, Integer eventId, String token)
            throws MessagingException, UnsupportedEncodingException {
        String surveyLink = "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw1/event/" + eventId + "?u=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = buildPostSurveyEmailContent(userName, eventName, eventId, surveyLink, token);

        helper.setFrom("noreply@eventhub.com", "EventHub Team");
        helper.setTo(to);
        helper.setSubject("How was " + eventName + "? Share your thoughts!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildPostSurveyEmailContent(String userName, String eventName, Integer eventId, String surveyLink, String token) {
        // String surveyLink = "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw1/event/" + eventId + "/survey/post" + "?u=" + userName;
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<style>"
                + "body {font-family: Arial, sans-serif; color: #333; background-color: #f6f9fc;}"
                + ".container {background-color: #ffffff; border-radius: 8px; padding: 40px; max-width: 600px; margin: 40px auto; box-shadow: 0 4px 6px rgba(0,0,0,0.1);}"
                + ".header {font-size: 24px; color: #1a1a1a; margin-bottom: 20px; font-weight: bold; text-align: center;}"
                + ".content {font-size: 16px; line-height: 1.6; color: #4a5568; margin-bottom: 30px;}"
                + ".btn-container {text-align: center; margin: 30px 0;}"
                + ".btn {background-color: #4f46e5; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px; display: inline-block;}"
                + ".footer {font-size: 12px; color: #a0aec0; margin-top: 40px; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 20px;}"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>We realized the event \"" + eventName + "\" has ended!</div>"
                + "<div class='content'>"
                + "<p>Hi " + userName + ",</p>"
                + "<p>We hope you had a great time at <strong>" + eventName + "</strong>.</p>"
                + "<p>We would love to hear your feedback to help us make future events even better. Please create a moment to fill out our quick survey.</p>"
                + "</div>"
                + "<div class='btn-container'>"
                + "<a href='" + surveyLink + "' class='btn' style='color: #ffffff !important;'>Take the Survey</a>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>Survey Link: <a href='" + surveyLink + "'>" + surveyLink + "</a></p>"
                + "&copy; 2026 EventHub. All rights reserved.<br>"
                + "This email was sent automatically. Please do not reply."
                + "</div>"
                + "</div>"
                + "</body>";
    }
}