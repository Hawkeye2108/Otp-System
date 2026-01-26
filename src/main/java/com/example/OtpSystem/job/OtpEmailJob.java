package com.example.OtpSystem.job;

import com.example.OtpSystem.service.EmailService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        emailService.sendMail(email, "Your OTP Code", "Your OTP is: " + otp);
    }
}
