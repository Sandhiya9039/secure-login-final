package com.secureapp.loginapp.controller;

import com.secureapp.loginapp.model.User;
import com.secureapp.loginapp.security.CustomUserDetails;
import com.secureapp.loginapp.service.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          LocationService locationService,
                          OtpService otpService,
                          EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.locationService = locationService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model, HttpServletRequest request) {
        if (!StringUtils.hasText(user.getUsername()) || !StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(user.getPassword())) {
            model.addAttribute("error", "All fields are required.");
            return "register";
        }
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already used.");
            return "register";
        }
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already used.");
            return "register";
        }

        // Hash password and save temporarily disabled (or enabled based on design)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // require OTP verification
        userService.save(user);

        // generate and send OTP
        String otp = otpService.generateOtpFor(user.getEmail());
        emailService.sendSimpleMail(user.getEmail(), "Your Registration OTP", "Your OTP is: " + otp);

        model.addAttribute("email", user.getEmail());
        return "verify";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        if (otpService.verifyOtp(email, otp)) {
            userService.findByEmail(email).ifPresent(u -> {
                u.setEnabled(true);
                userService.save(u);
            });
            model.addAttribute("message", "Verification successful. Please login.");
            return "login";
        } else {
            model.addAttribute("error", "Invalid or expired OTP.");
            model.addAttribute("email", email);
            return "verify";
        }
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/home")
    public String homePage(Model model, Authentication authentication, HttpServletRequest request) {
        String ip = extractClientIp(request);
        String username = extractUsername(authentication);
        model.addAttribute("username", username);
        model.addAttribute("ip", ip);

        // check geofence
        boolean allowed = locationService.isIpAllowed(ip);
        if (!allowed) {
            // send OTP fallback (login from unknown location)
            userService.findByUsername(username).ifPresent(u -> {
                String otp = otpService.generateOtpFor(u.getEmail());
                emailService.sendSimpleMail(u.getEmail(), "Login OTP", "Your login OTP is: " + otp);
            });
            model.addAttribute("needsOtp", true);
            model.addAttribute("email", userService.findByUsername(username).map(u -> u.getEmail()).orElse(""));
            return "verify";
        } else {
            // update last login IP
            userService.updateLastLoginIp(username, ip);
            model.addAttribute("welcome", "Welcome to Secure GeoLogin App");
            return "home";
        }
    }

    @PostMapping("/verify-login-otp")
    public String verifyLoginOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        if (otpService.verifyOtp(email, otp)) {
            // OTP ok — you might set a session flag or allow access. For simplicity redirect to home.
            model.addAttribute("message", "OTP verified, you are logged in.");
            return "home";
        } else {
            model.addAttribute("error", "Invalid or expired OTP.");
            model.addAttribute("email", email);
            return "verify";
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && header.length() != 0) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUsername(Authentication authentication) {
        if (authentication == null) return "";
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) return cud.getUser().getUsername();
        return authentication.getName();
    }
}
