package com.example.OtpSystem.job;

import com.example.OtpSystem.service.EmailService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sendinblue.ApiException;

@Component
public class OtpEmailJob implements Job {
    @Autowired
    private EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Get data passed to this job
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String email = dataMap.getString("email");
        String otp = dataMap.getString("otp");

        // Send the OTP email
        try {
            String htmlContent =
                    "<html>" +
                            "<body style='font-family: Arial, sans-serif;'>" +
                            "<h2>OTP Verification</h2>" +
                            "<p>Your One-Time Password (OTP) is:</p>" +
                            "<h1 style='color: #2E86C1; letter-spacing: 5px;'>" + otp + "</h1>" +
                            "<p><b>This OTP is valid for 5 minutes.</b></p>" +
                            "<p>If you did not request this, please ignore this email.</p>" +
                            "</body>" +
                            "</html>";
            emailService.sendMail(email, "Your OTP Code", htmlContent);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }
}
