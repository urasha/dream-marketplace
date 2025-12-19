package ru.urasha.callmeani.dream_marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(AppS3Properties props) {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.accessKey(), props.secretKey())
                ))
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.region()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
