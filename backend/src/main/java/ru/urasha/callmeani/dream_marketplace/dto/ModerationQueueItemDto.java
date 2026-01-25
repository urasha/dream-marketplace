package ru.urasha.callmeani.dream_marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ModerationQueueItemDto(
        Long id,
        String title,
        String description,
        String authorName,
        LocalDateTime submittedAt,
        BigDecimal price,
        List<String> tags,
        String previewUrl
) {
}