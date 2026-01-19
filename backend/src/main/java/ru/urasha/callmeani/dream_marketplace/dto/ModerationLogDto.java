package ru.urasha.callmeani.dream_marketplace.dto;

import java.time.LocalDateTime;

public record ModerationLogDto(
        Long id,
        Long lotId,
        String lotTitle,
        String moderatorName,
        String action,
        String reason,
        LocalDateTime createdAt
) {
}