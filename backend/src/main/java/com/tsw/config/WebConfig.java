package com.tsw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS is configured in SecurityConfig via CorsConfigurationSource
@Configuration
public class WebConfig implements WebMvcConfigurer {
}
