package ru.urasha.callmeani.dream_marketplace.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        BigDecimal amount,
        PaymentStatus status,
        String confirmationUrl,
        boolean credited,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
