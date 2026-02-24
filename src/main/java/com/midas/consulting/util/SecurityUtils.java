package com.midas.consulting.util;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SecurityUtils {
    public String getJwtToken() {


//        if (authentication != null && authentication.getCredentials()) {
//            Jwt jwt = (Jwt) authentication.getCredentials();
//            return jwt.getTokenValue();  // The actual JWT token as a string
//        }

        return null;
    }
    public static String getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> list= authentication.getAuthorities();
      return (String)authentication.getPrincipal();

    }
}
