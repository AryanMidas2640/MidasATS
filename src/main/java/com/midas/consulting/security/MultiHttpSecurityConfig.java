package com.midas.consulting.security;

import com.midas.consulting.security.api.ApiJWTAuthenticationFilter;
import com.midas.consulting.security.api.ApiJWTAuthorizationFilter;
import com.midas.consulting.security.api.IpWhitelistFilter;
import com.midas.consulting.security.api.TenantWithTokenFilter;
import com.midas.consulting.security.form.CustomAuthenticationSuccessHandler;
import com.midas.consulting.security.form.CustomLogoutSuccessHandler;
import com.midas.consulting.service.WhitelistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Dheeraj Singh.
 */

@EnableWebSecurity
public class MultiHttpSecurityConfig {

//    @Configuration
//    @Order(1)
//    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
//
//        @Autowired
//        private TenantWithTokenFilter tenantWithTokenFilter;
//        @Autowired
//        private BCryptPasswordEncoder bCryptPasswordEncoder;
//        @Autowired
//        private WhitelistService ipWhitelistService; // Changed: inject service instead of filter
////        private IpWhitelistService ipWhitelistService; // Use the correct interface
//
//        @Autowired
//        private CustomUserDetailsService userDetailsService;
//
//        // Create IpWhitelistFilter as a bean
//        @Bean
//        public IpWhitelistFilter ipWhitelistFilter(WhitelistService ipWhitelistService) {
//            return new IpWhitelistFilter(ipWhitelistService);
//        }
//
//        @Bean
//        public IpWhitelistFilter ipWhitelistFilter() {
//            return new IpWhitelistFilter();
//        }
//
//        @Override
//        @Bean
//        public AuthenticationManager authenticationManagerBean() throws Exception {
//            return super.authenticationManagerBean();
//        }
//
//        @Override
//        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//            auth
//                    .userDetailsService(userDetailsService)
//                    .passwordEncoder(bCryptPasswordEncoder);
//        }
//
//        // @formatter:off
//        protected void configure(HttpSecurity http) throws Exception {
//            http .cors()
//                    .and()
//                    .csrf()
//                    .disable()
//                    .antMatcher("/api/v1/**")
//                    .authorizeRequests()
//                    .antMatchers("/api/v1/user/**").permitAll()
//                    .antMatchers("/api/v1/user/authenticate").permitAll()
//                    .antMatchers("/api/v1/user/signup").permitAll()
//                    .antMatchers("/api/v1/notifications/**").permitAll()
//                    .antMatchers("/api/v1/user/getUserById").permitAll()
//                    .anyRequest()
//                    .authenticated()
//                    .and()
//                    .exceptionHandling()
//                    .authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
//                    .and()
////                    .addFilterBefore(ipWhitelistFilter(), TenantWithTokenFilter.class) // Add IP whitelist filter first
//                    .addFilterBefore(tenantWithTokenFilter, ApiJWTAuthenticationFilter.class)
//                    .addFilterBefore(ipWhitelistFilter(), TenantWithTokenFilter.class)
//                    .addFilter(new ApiJWTAuthenticationFilter(authenticationManager()))
//                    .addFilter(new ApiJWTAuthorizationFilter(authenticationManager()))
//                    .sessionManagement()
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        }
//    }
@Configuration
@Order(1)
public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private WhitelistService ipWhitelistService;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public IpWhitelistFilter ipWhitelistFilter() {
        return new IpWhitelistFilter(ipWhitelistService);
    }

    @Bean
    public TenantWithTokenFilter tenantWithTokenFilter() {
        return new TenantWithTokenFilter();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .antMatcher("/api/v1/**")
                .authorizeRequests()
                .antMatchers("/api/v1/user/**").permitAll()
                .antMatchers("/api/v1/user/authenticate").permitAll()
                .antMatchers("/api/v1/user/signup").permitAll()
                .antMatchers("/api/v1/notifications/**").permitAll()
                .antMatchers("/api/v1/tenants/**").permitAll()
                .antMatchers("/api/v1/user/getUserById").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                // Use the bean instances, not class references
                .addFilterBefore(ipWhitelistFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantWithTokenFilter(), ApiJWTAuthenticationFilter.class)
                .addFilter(new ApiJWTAuthenticationFilter(authenticationManager()))
                .addFilter(new ApiJWTAuthorizationFilter(authenticationManager()))
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
    @Order(2)
    @Configuration
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Autowired
        private BCryptPasswordEncoder bCryptPasswordEncoder;

        @Autowired
        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(bCryptPasswordEncoder);
        }

        // @formatter:off
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .cors()
                    .and()
                    .csrf()
                    .disable()
                    .authorizeRequests()
                    .antMatchers("/").permitAll()
                    .antMatchers("/login").permitAll()
                    .antMatchers("/signup").permitAll()
                    // Add Swagger endpoints here
                    .antMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/v2/api-docs",
                            "/webjars/**",
                            "/swagger-resources/**",
                            "/configuration/**"
                    ).permitAll()
                    .anyRequest()
                    .authenticated()
                    .and()
                    .formLogin()
                    .loginPage("/login")
                    .successHandler(customAuthenticationSuccessHandler)
                    .permitAll()
                    .and()
                    .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(new CustomLogoutSuccessHandler());
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/static/**", "/css/**", "/js/**", "/images/**",
                    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v2/api-docs",
                    "/webjars/**", "/swagger-resources/**", "/configuration/**");
        }
    }
}

//package com.midas.consulting.security;
//
//import com.midas.consulting.security.api.ApiJWTAuthenticationFilter;
//import com.midas.consulting.security.api.IpWhitelistFilter;
//import com.midas.consulting.security.api.TenantWithTokenFilter;
//import com.midas.consulting.security.form.CustomAuthenticationSuccessHandler;
//import com.midas.consulting.security.form.CustomLogoutSuccessHandler;
//import com.midas.consulting.security.api.ApiJWTAuthorizationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
//import javax.servlet.http.HttpServletResponse;
//
///**
// * Created by Dheeraj Singh.
// */
//
//@EnableWebSecurity
//public class MultiHttpSecurityConfig {
//
//
//    @Configuration
//    @Order(1)
//    public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
//
//        @Autowired
//        private TenantWithTokenFilter tenantWithTokenFilter;
//
//
//        @Autowired
//        private BCryptPasswordEncoder bCryptPasswordEncoder;
//        @Autowired
//        private IpWhitelistFilter ipWhitelistFilter; // Add this
//        @Autowired
//        private CustomUserDetailsService userDetailsService;
//
//
//
//        @Override
//        @Bean
//        public AuthenticationManager authenticationManagerBean() throws Exception {
//            return super.authenticationManagerBean();
//        }
//
//        @Override
//        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//            auth
//                    .userDetailsService(userDetailsService)
//                    .passwordEncoder(bCryptPasswordEncoder);
//        }
//
//        // @formatter:off
//        protected void configure(HttpSecurity http) throws Exception {
//            http .cors()
//                    .and()
//                    .csrf()
//                    .disable()
//                    .antMatcher("/api/v1/**")
//                    .authorizeRequests()
//                    .antMatchers("/api/v1/user/**").permitAll()
//                    .antMatchers("/api/v1/user/authenticate").permitAll()
//                    .antMatchers("/api/v1/user/signup").permitAll()
//                    .antMatchers("/api/v1/notifications/**").permitAll()
//                    .antMatchers("/api/v1/user/getUserById").permitAll()
//
//                    .anyRequest()
//                    .authenticated()
//                    .and()
//                    .exceptionHandling()
//                    .authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
//                    .and()
//                    .addFilterBefore(ipWhitelistFilter, TenantWithTokenFilter.class) // Add this line
//                    .addFilterBefore(tenantWithTokenFilter, ApiJWTAuthenticationFilter.class)
//                    .addFilter(new ApiJWTAuthenticationFilter(authenticationManager()))
//                    .addFilter(new ApiJWTAuthorizationFilter(authenticationManager()))
//                    .sessionManagement()
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//
//        }
//    }
//
//    @Order(2)
//    @Configuration
//    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
//        @Autowired
//        private BCryptPasswordEncoder bCryptPasswordEncoder;
//
//        @Autowired
//        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
//
//        @Autowired
//        private CustomUserDetailsService userDetailsService;
//
//        @Override
//        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//            auth
//                    .userDetailsService(userDetailsService)
//                    .passwordEncoder(bCryptPasswordEncoder);
//        }
//
//        // @formatter:off
//        @Override
//        protected void configure(HttpSecurity http) throws Exception {
//            http
//                    .cors()
//                    .and()
//                    .csrf()
//                    .disable()
//                    .authorizeRequests()
//                    .antMatchers("/").permitAll()
//                    .antMatchers("/login").permitAll()
//                    .antMatchers("/signup").permitAll()
//                    // Add Swagger endpoints here
//                    .antMatchers(
//                            "/swagger-ui/**",
//                            "/swagger-ui.html",
//                            "/v3/api-docs/**",
//                            "/v2/api-docs",
//                            "/webjars/**",
//                            "/swagger-resources/**",
//                            "/configuration/**"
//                    ).permitAll()
//                    .antMatchers("/dashboard/**").hasAuthority("ADMIN")
//                    .anyRequest()
//                    .authenticated()
//                    .and()
//                    .formLogin()
//                    .loginPage("/login")
//                    .permitAll()
//                    .failureUrl("/login?error=true")
//                    .usernameParameter("email")
//                    .passwordParameter("password")
//                    .successHandler(customAuthenticationSuccessHandler)
//                    .and()
//                    .logout()
//                    .permitAll()
//                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                    .logoutSuccessHandler(new CustomLogoutSuccessHandler())
//                    .deleteCookies("JSESSIONID")
//                    .logoutSuccessUrl("/")
//                    .and()
//                    .exceptionHandling();
//        }
//
//        @Override
//        public void configure(WebSecurity web) throws Exception {
//            web.ignoring().antMatchers(
//                    "/v2/api-docs",
//                    "/configuration/ui",
//                    "/swagger-resources/**",
//                    "/configuration/security",
//                    "/swagger-ui.html",
//                    "/swagger-ui/**",
//                    "/webjars/**",
//                    "/resources/**", "/static/**", "/css/**", "/js/**", "/images/**",
//                    "/resources/static/**", "/css/**", "/js/**", "/img/**", "/fonts/**",
//                    "/images/**", "/scss/**", "/vendor/**", "/favicon.ico", "/auth/**", "/favicon.png",
//                    "/v2/api-docs", "/configuration/ui", "/configuration/security",
//                    "/webjars/**", "/swagger-resources/**", "/actuator", "/swagger-ui/**","/api/v1/user/signup",
//                    "/actuator/**", "/swagger-ui/index.html", "/swagger-ui/");
//        }
//    }
//
//
//}