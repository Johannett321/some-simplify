package com.somesimplify.somesimplify.config;

import com.somesimplify.somesimplify.enums.Environment;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(value = "somesimplify")
public class ApplicationConfig {
    private String backendUrl;
    private String frontendUrl;
    private String jwtSecretKey;
    private Environment environment;
    private String contactEmail;
}
