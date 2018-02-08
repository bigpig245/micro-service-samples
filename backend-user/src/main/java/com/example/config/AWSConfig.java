package com.example.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "s3.media.mock", havingValue = "false")
@Profile("!local")
public class AWSConfig {
    @Bean
    @Primary
    public AWSCredentialsProvider awsCredentialsProvider() {
        log.info("Create DefaultAWSCredentialsProviderChain");
        DefaultAWSCredentialsProviderChain credentialsProvider = new DefaultAWSCredentialsProviderChain();
        AWSCredentials credentials = credentialsProvider.getCredentials();
        log.info("AWS Credentials: {}, accessKey {}", credentials, credentials.getAWSAccessKeyId());
        return credentialsProvider;
    }
}