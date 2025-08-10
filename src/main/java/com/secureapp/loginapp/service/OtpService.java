package com.secureapp.loginapp.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, OtpEntry> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtpFor(String email) {
        int otp = 100000 + random.nextInt(900000);
        String code = String.valueOf(otp);
        otpCache.put(email, new OtpEntry(code, Instant.now().plusSeconds(300))); // 5 min
        return code;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry entry = otpCache.get(email);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt)) {
            otpCache.remove(email);
            return false;
        }
        boolean ok = entry.code.equals(otp);
        if (ok) otpCache.remove(email);
        return ok;
    }

    private static class OtpEntry {
        final String code;
        final Instant expiresAt;
        OtpEntry(String code, Instant expiresAt) { this.code = code; this.expiresAt = expiresAt; }
    }
}
