package com.templateapp.templateapp.config;

import com.templateapp.templateapp.enums.Environment;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(value = "templateapp")
public class ApplicationConfig {
    private String backendUrl;
    private String frontendUrl;
    private String jwtSecretKey;
    private Environment environment;
    private String contactEmail;
}
