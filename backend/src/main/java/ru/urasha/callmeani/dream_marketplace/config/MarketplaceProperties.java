package ru.urasha.callmeani.dream_marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.marketplace")
public class MarketplaceProperties {
    /**
     * Commission percent taken by the platform from each sale (e.g. 1.25 for 1.25%).
     */
    private BigDecimal feePercent = BigDecimal.ZERO;

    public BigDecimal getFeePercent() {
        return feePercent;
    }

    public void setFeePercent(BigDecimal feePercent) {
        this.feePercent = feePercent;
    }
}