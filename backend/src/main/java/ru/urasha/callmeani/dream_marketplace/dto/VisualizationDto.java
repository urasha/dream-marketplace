package ru.urasha.callmeani.dream_marketplace.dto;

import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;

import java.time.LocalDateTime;

public record VisualizationDto(Long id,
                               String prompt,
                               String generator,
                               String filePath,
                               String mime,
                               Integer width,
                               Integer height,
                               Integer duration,
                               VisualizationStatus status,
                               Long dreamRecordId,
                               LocalDateTime createdAt) {
}
