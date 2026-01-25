package ru.urasha.callmeani.dream_marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseItemDto(
        Long id,
        Long lotId,
        String lotTitle,
        String lotDescription,
        BigDecimal amount,
        LocalDateTime purchasedAt,
        String visualizationUrl,
        Long authorId,
        String authorName
) {
}