package com.midas.consulting.service;

import com.midas.consulting.model.IpWhitelistType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.InetAddress;

@Service
public class IpValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IpValidationService.class);
    
    public boolean isValidIpAddress(String ipAddress) {
        if (StringUtils.isEmpty(ipAddress)) {
            return false;
        }
        
        // Handle wildcard
        if ("*".equals(ipAddress)) {
            return true;
        }
        
        try {
            // Check if it's a CIDR range
            if (ipAddress.contains("/")) {
                String[] parts = ipAddress.split("/");
                if (parts.length != 2) return false;
                
                InetAddress.getByName(parts[0]); // Validate IP part
                int prefix = Integer.parseInt(parts[1]);
                return prefix >= 0 && prefix <= 32;
            } else {
                // Single IP validation
                InetAddress.getByName(ipAddress);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Invalid IP address format: {}", ipAddress);
            return false;
        }
    }
    
    public boolean isIpInRange(String clientIp, String allowedIp, IpWhitelistType type) {
        try {
            switch (type) {
                case WILDCARD:
                    return "*".equals(allowedIp);
                    
                case SINGLE_IP:
                    return clientIp.equals(allowedIp);
                    
                case IP_RANGE:
                    return isIpInCidrRange(clientIp, allowedIp);
                    
                default:
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error checking IP range: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isIpInCidrRange(String clientIp, String cidrRange) {
        try {
            String[] parts = cidrRange.split("/");
            if (parts.length != 2) return false;
            
            InetAddress targetAddr = InetAddress.getByName(clientIp);
            InetAddress rangeAddr = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            
            byte[] targetBytes = targetAddr.getAddress();
            byte[] rangeBytes = rangeAddr.getAddress();
            
            int bytesToCheck = prefixLength / 8;
            int bitsToCheck = prefixLength % 8;
            
            // Check full bytes
            for (int i = 0; i < bytesToCheck; i++) {
                if (targetBytes[i] != rangeBytes[i]) {
                    return false;
                }
            }
            
            // Check remaining bits
            if (bitsToCheck > 0 && bytesToCheck < targetBytes.length) {
                int mask = 0xFF << (8 - bitsToCheck);
                return (targetBytes[bytesToCheck] & mask) == (rangeBytes[bytesToCheck] & mask);
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error validating CIDR range: {}", e.getMessage());
            return false;
        }
    }
}
