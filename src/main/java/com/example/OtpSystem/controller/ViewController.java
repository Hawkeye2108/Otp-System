package com.example.OtpSystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    /**
     * Serve the OTP HTML page
     * GET /
     */
    @GetMapping("/")
    public String otpPage() {
        return "otp";  // This will serve otp.html from templates folder
    }
}
