package com.secureapp.loginapp.controller;

import com.secureapp.loginapp.model.User;
import com.secureapp.loginapp.repository.UserRepository;
import com.secureapp.loginapp.service.OTPService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class OTPController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OTPService otpService;

    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email, HttpSession session, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            otpService.generateAndSendOTP(user);
            session.setAttribute("otpUser", user.getEmail());
            return "verify";
        } else {
            model.addAttribute("error", "Email not found");
            return "login";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String otp, HttpSession session, Model model) {
        String email = (String) session.getAttribute("otpUser");
        if (email == null) {
            model.addAttribute("error", "Session expired");
            return "login";
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (otpService.verifyOTP(user, otp)) {
                session.setAttribute("loggedInUser", user);
                return "redirect:/welcome";
            } else {
                model.addAttribute("error", "Invalid or expired OTP");
                return "verify";
            }
        }

        model.addAttribute("error", "User not found");
        return "login";
    }
}
