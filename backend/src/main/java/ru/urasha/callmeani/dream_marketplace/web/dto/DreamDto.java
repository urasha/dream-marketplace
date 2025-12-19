package ru.urasha.callmeani.dream_marketplace.web.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;

import java.time.LocalDateTime;
import java.util.Set;

public record DreamDto(Long id,
                       String title,
                       String content,
                       Privacy privacy,
                       Long categoryId,
                       Set<Long> tagIds,
                       Long visualizationId,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
}
