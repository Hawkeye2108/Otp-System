package com.example.OtpSystem.job;

import com.example.OtpSystem.service.OtpService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OtpCleanupJob implements Job {
    @Autowired
    private OtpService otpService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        otpService.cleanupExpiredOtps();
    }
}
