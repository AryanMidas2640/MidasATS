package com.midas.consulting.config;

import com.midas.consulting.security.api.IpWhitelistFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration class to register the IP Whitelist Filter
 */
@Configuration
public class IpWhitelistFilterConfig {

    @Autowired
    private IpWhitelistFilter ipWhitelistFilter;

    /**
     * Register the IP whitelist filter with high priority
     * This filter should run after authentication but before authorization
     */
    @Bean
    public FilterRegistrationBean<IpWhitelistFilter> ipWhitelistFilterRegistration() {
        FilterRegistrationBean<IpWhitelistFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(ipWhitelistFilter);
        
        // Set URL patterns to apply the filter
        registration.addUrlPatterns("/api/*", "/v1/*", "/admin/*");
        
        // Set filter name
        registration.setName("ipWhitelistFilter");
        
        // Set order - should be after security filters but before business logic
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        
        return registration;
    }
}