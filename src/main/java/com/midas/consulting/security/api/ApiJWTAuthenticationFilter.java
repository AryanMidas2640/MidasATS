package com.midas.consulting.security.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.midas.consulting.security.SecurityConstants.*;

/**
 * Created by Dheeraj Singh.
 */
public class ApiJWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    private static final String TENANT_HEADER = "X-Tenant";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017/TENANT?readPreference=primary";
    private static final String TENANT_REPLACEMENT = "TENANT";

    private static final Logger logger = LoggerFactory.getLogger(ApiJWTAuthenticationFilter.class);



    public ApiJWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/v1/user/signup", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
        try {
            User user = new ObjectMapper().readValue(req.getInputStream(), User.class);
            if (!user.isActive()){
                throw new MidasCustomException.UserNotActiveException("Please activate your account you must have received a mail stating the same");
            }
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        if (auth.getPrincipal() != null) {
            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
            String login = user.getUsername();
            if (login != null && login.length() > 0) {
                Claims claims = Jwts.claims().setSubject(login);
                List<String> roles = new ArrayList<>();
                user.getAuthorities().stream().forEach(authority -> roles.add(authority.getAuthority()));
                claims.put("roles", roles);
                String token = Jwts.builder()
                        .setClaims(claims)
                        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                        .signWith(SignatureAlgorithm.HS512, SECRET)
                        .compact();
                res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
                res.getWriter().write(token);
            }
        }
    }
}
