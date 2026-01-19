package ru.urasha.callmeani.dream_marketplace.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
	YandexOAuthProperties.class,
	JwtProperties.class,
	AppS3Properties.class,
	GenApiProperties.class,
	VisualizationProperties.class,
	YooKassaProperties.class,
	MarketplaceProperties.class
})
public class PropertiesConfig {
}
