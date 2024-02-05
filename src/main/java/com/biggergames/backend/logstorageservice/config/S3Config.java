package com.biggergames.backend.logstorageservice.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Getter
@Setter
@RequiredArgsConstructor
public class S3Config {

    @Value("${storage.s3.region}")
    private String region;

    @Value("${storage.s3.bucket}")
    private String bucketName;

    @Value("${storage.s3.key}")
    private String key;

    @Value("${bg.aws.s3.access}")
    private String accessKey;

    @Value("${bg.aws.s3.secret}")
    private String secretKey;

    @Value("${format.date-of-save:yyyy-MM-dd__HH.mm.ss}")
    private String saveDateFormat;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                ).build();
    }
}
