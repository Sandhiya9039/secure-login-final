package com.secureapp.loginapp.service;

import com.secureapp.loginapp.model.User;
import com.secureapp.loginapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OTPService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public void generateAndSendOTP(User user) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpGeneratedAt(LocalDateTime.now());
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    public boolean verifyOTP(User user, String inputOtp) {
        if (user.getOtp() == null || user.getOtpGeneratedAt() == null) {
            return false;
        }

        LocalDateTime expiryTime = user.getOtpGeneratedAt().plusMinutes(5);
        return user.getOtp().equals(inputOtp) && LocalDateTime.now().isBefore(expiryTime);
    }
}
