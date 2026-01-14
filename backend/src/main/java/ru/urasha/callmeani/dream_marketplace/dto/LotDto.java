package ru.urasha.callmeani.dream_marketplace.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LotDto(
        Long id,
        Long dreamId,
        Long visualizationId,
        String title,
        String description,
        BigDecimal price,
        LotStatus status,
        Long authorId,
        String authorName,
        Long categoryId,
        String categoryName,
        java.util.List<String> tags,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String moderationReason
) {
}
