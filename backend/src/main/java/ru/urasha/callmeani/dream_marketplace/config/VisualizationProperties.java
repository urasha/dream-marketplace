package ru.urasha.callmeani.dream_marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.visualization")
public class VisualizationProperties {
    private boolean mockEnabled = true;
    private String mockImageUrl = "https://placehold.co/640x640/EEE/222?text=Mock+image";

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public String getMockImageUrl() {
        return mockImageUrl;
    }

    public void setMockImageUrl(String mockImageUrl) {
        this.mockImageUrl = mockImageUrl;
    }
}
