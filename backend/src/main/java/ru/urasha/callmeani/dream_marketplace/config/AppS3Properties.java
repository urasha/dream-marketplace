package ru.urasha.callmeani.dream_marketplace.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.s3")
public class AppS3Properties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String region;

    @NotBlank
    private String bucket;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    public String endpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String region() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String accessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String secretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
