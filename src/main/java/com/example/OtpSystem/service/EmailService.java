package com.example.OtpSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // Add sender email from properties
    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendMail(String to, String subject, String body){
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);

        mailSender.send(mail);
    }
}
