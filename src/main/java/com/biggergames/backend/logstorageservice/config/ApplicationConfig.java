package com.biggergames.backend.logstorageservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ApplicationConfig {
    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${bg-time.format:yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ}")
    private String bgTimeFormat;
}
