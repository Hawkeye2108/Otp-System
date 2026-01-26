package com.example.OtpSystem.service;

import com.example.OtpSystem.job.OtpEmailJob;
import com.example.OtpSystem.model.Otp;
import com.example.OtpSystem.repository.OtpRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class OtpService {
    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_VERIFICATION_ATTEMPTS = 3;
    private static final int EMAIL_DELAY_SECONDS = 0;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Generates OTP and schedules email to be sent after 20 seconds
     */
    @Transactional
    public String generateOtp(String email) throws SchedulerException {
        // Invalidate any existing unverified OTPs for this email
        invalidateExistingOtps(email);

        // Generate secure 6-digit OTP
        String otpCode = generateSecureOtp();

        // Calculate expiry time (5 minutes from now)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(OTP_VALIDITY_MINUTES);

        // Save OTP to database
        Otp otp = new Otp(email, otpCode, now, expiryTime);
        otpRepository.save(otp);

        // Schedule email to be sent after 20 seconds
        scheduleOtpEmail(email, otpCode);

        return otpCode;
    }

    /**
     * Verifies the OTP entered by user
     */
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        // Find the latest unverified OTP for this email
        Optional<Otp> otpOptional = otpRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email);

        if (otpOptional.isEmpty()) {
            return false; // No OTP found
        }

        Otp otp = otpOptional.get();

        // Check if OTP has expired
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            return false; // OTP expired
        }

        // Check if max attempts exceeded
        if (otp.getAttemptCount() >= MAX_VERIFICATION_ATTEMPTS) {
            return false; // Too many failed attempts
        }

        // Increment attempt count
        otp.setAttemptCount(otp.getAttemptCount() + 1);

        // Check if OTP matches
        if (otp.getOtpCode().equals(otpCode)) {
            otp.setVerified(true);
            otpRepository.save(otp);
            return true; // Success!
        }

        otpRepository.save(otp);
        return false; // Wrong OTP
    }

    /**
     * Invalidates all existing unverified OTPs for an email
     * This prevents users from having multiple active OTPs
     */
    private void invalidateExistingOtps(String email) {
        Optional<Otp> existingOtp = otpRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email);
        existingOtp.ifPresent(otp -> {
            otp.setVerified(true); // Mark as verified to invalidate
            otpRepository.save(otp);
        });
    }

    /**
     * Generates a cryptographically secure 6-digit OTP
     */
    private String generateSecureOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Schedules OTP email to be sent after specified delay
     */
    private void scheduleOtpEmail(String email, String otpCode) throws SchedulerException {
        String jobId = "otpJob-" + email + "-" + System.currentTimeMillis();
        String triggerId = "otpTrigger-" + email + "-" + System.currentTimeMillis();

        // Create job data
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", email);
        jobDataMap.put("otp", otpCode);

        // Create job
        JobDetail jobDetail = JobBuilder.newJob(OtpEmailJob.class)
                .withIdentity(jobId)
                .usingJobData(jobDataMap)
                .build();

        // Schedule to run after EMAIL_DELAY_SECONDS seconds
        Date runTime = new Date(System.currentTimeMillis() + (EMAIL_DELAY_SECONDS * 1000));

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerId)
                .startAt(runTime)
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Cleans up expired OTPs from database
     * Should be called periodically by a scheduled job
     */
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    /**
     * Gets remaining validity time for an OTP in seconds
     */
    public long getRemainingValiditySeconds(String email) {
        Optional<Otp> otpOptional = otpRepository.findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(email);

        if (otpOptional.isEmpty()) {
            return 0;
        }

        Otp otp = otpOptional.get();
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(otp.getExpiresAt())) {
            return 0;
        }

        return java.time.Duration.between(now, otp.getExpiresAt()).getSeconds();
    }
}
