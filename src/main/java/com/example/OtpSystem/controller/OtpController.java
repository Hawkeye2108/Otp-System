package com.example.OtpSystem.controller;

import com.example.OtpSystem.dto.ApiResponse;
import com.example.OtpSystem.dto.OtpRequestDto;
import com.example.OtpSystem.dto.OtpVerifyDto;
import com.example.OtpSystem.repository.OtpRepository;
import com.example.OtpSystem.service.OtpService;
import jakarta.validation.Valid;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private OtpRepository otpRepository;

    /**
     * Serve the OTP HTML page
     * GET /api/otp/page
     */
    @GetMapping("/page")
    public String otpPage() {
        return "otp";  // This will serve otp.html from templates folder
    }

    /**
     * Generate and send OTP
     * POST /api/otp/generate
     */
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<ApiResponse> generateOtp(@Valid @RequestBody OtpRequestDto request) {
        try {
            String otp = otpService.generateOtp(request.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "OTP has been sent to your email. It is valid for 5 minutes."
                    )
            );
        } catch (SchedulerException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to generate OTP: " + e.getMessage()));
        }
    }

    /**
     * Verify OTP
     * POST /api/otp/verify
     */
    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (isValid) {
            return ResponseEntity.ok(
                    new ApiResponse(true, "OTP verified successfully!")
            );
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid or expired OTP. Please try again."));
        }
    }

    /**
     * Check remaining validity time
     * GET /api/otp/validity?email=user@example.com
     */
    @GetMapping("/validity")
    @ResponseBody
    public ResponseEntity<ApiResponse> checkValidity(@RequestParam String email) {
        long remainingSeconds = otpService.getRemainingValiditySeconds(email);

        if (remainingSeconds > 0) {
            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "OTP is still valid",
                            "Remaining time: " + remainingSeconds + " seconds"
                    )
            );
        } else {
            return ResponseEntity.ok(
                    new ApiResponse(false, "No valid OTP found or OTP has expired")
            );
        }
    }

    @GetMapping("/test-db")
    @ResponseBody
    public ResponseEntity<ApiResponse> testDatabase() {
        try {
            long count = otpRepository.count();
            return ResponseEntity.ok(
                    new ApiResponse(
                            true,
                            "Database connection successful!",
                            "Total OTPs in database: " + count
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Database connection failed: " + e.getMessage()));
        }
    }
}
