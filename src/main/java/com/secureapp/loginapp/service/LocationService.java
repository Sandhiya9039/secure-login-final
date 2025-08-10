package com.secureapp.loginapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Simple IP-based allowed list service. Replace with a geolocation API for real geofencing.
 */
@Service
public class LocationService {

    private final List<String> allowedIpPrefixes;

    public LocationService(@Value("${geologin.allowed.ip.prefixes:127.0.0.1,::1}") String prefixes) {
        this.allowedIpPrefixes = Arrays.stream(prefixes.split(",")).map(String::trim).toList();
    }

    /**
     * Returns true if the ip matches any allowed prefix (exact or startsWith).
     */
    public boolean isIpAllowed(String ip) {
        if (ip == null) return false;
        for (String p : allowedIpPrefixes) {
            if (p.isEmpty()) continue;
            if (ip.equals(p) || ip.startsWith(p)) return true;
        }
        return false;
    }
}
