package ru.urasha.callmeani.dream_marketplace.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.genapi")
public class GenApiProperties {

    @NotBlank
    private String baseUrl;

    @NotBlank
    private String apiKey;

    private int connectTimeoutMillis = 5000;
    private int readTimeoutMillis = 30000;
}
