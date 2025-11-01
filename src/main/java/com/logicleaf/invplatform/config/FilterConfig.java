package com.logicleaf.invplatform.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    private final StartupIdFilter startupIdFilter;

    public FilterConfig(StartupIdFilter startupIdFilter) {
        this.startupIdFilter = startupIdFilter;
    }

    @Bean
    public FilterRegistrationBean<StartupIdFilter> startupIdFilterRegistration() {
        FilterRegistrationBean<StartupIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(startupIdFilter);
        // This URL pattern should cover all endpoints that need the startupId for Zoho API calls.
        registration.addUrlPatterns("/api/financials/*");
        registration.setName("startupIdFilter");
        registration.setOrder(1);
        return registration;
    }
}
