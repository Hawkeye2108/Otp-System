package com.example.OtpSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibApi.ApiClient;
import sibApi.Configuration;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    // Add sender email from properties
    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${brevo.api.key}")
    private String apiKey;

    public void sendMail(String to, String subject, String body){
//        SimpleMailMessage mail = new SimpleMailMessage();
//        mail.setFrom(fromEmail);
//        mail.setTo(to);
//        mail.setSubject(subject);
//        mail.setText(body);
//
//        mailSender.send(mail);

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        SendSmtpEmail email = new SendSmtpEmail();

        email.setSubject(subject);
        email.setHtmlContent("<html><body>" + body + "</body></html>");
        email.setTo(List.of(new SendSmtpEmailTo().email(to)));
        email.setSender(new SendSmtpEmailSender()
                .email("regularguy2108@gmail.com")
                .name("Otp System"));

        apiInstance.sendTransacEmail(email);
    }
}
