//package com.example.community_service.config;
//
//import com.example.community_service.filter.InternalApiFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class FilterConfig {
//
//    @Bean
//    public FilterRegistrationBean<InternalApiFilter> loggingFilter(InternalApiFilter filter) {
//        FilterRegistrationBean<InternalApiFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(filter);
//        registrationBean.addUrlPatterns("/api/*"); // Protect all API endpoints
//        registrationBean.setOrder(1); // Set highest priority
//        return registrationBean;
//    }
//}