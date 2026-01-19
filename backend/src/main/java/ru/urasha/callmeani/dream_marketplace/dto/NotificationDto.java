package ru.urasha.callmeani.dream_marketplace.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String message,
        Boolean isRead,
        LocalDateTime createdAt,
        Long targetLotId,
        Long targetCommentId
) {
}
