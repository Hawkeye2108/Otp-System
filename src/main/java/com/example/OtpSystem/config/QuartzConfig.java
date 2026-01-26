package com.example.OtpSystem.config;

import com.example.OtpSystem.job.OtpCleanupJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    // Cleanup job to remove expired OTPs every hour
    @Bean
    public JobDetail otpCleanupJobDetail() {
        return JobBuilder.newJob(OtpCleanupJob.class)
                .withIdentity("otpCleanupJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger otpCleanupTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInHours(1) // Run every hour
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(otpCleanupJobDetail())
                .withIdentity("otpCleanupTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}
