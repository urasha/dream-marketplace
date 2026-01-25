package ru.urasha.callmeani.dream_marketplace.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long userId,
        String username,
        String content,
        LocalDateTime createdAt
) {
}
