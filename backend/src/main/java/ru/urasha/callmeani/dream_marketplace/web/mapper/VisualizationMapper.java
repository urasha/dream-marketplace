package ru.urasha.callmeani.dream_marketplace.web.mapper;

import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;
import ru.urasha.callmeani.dream_marketplace.web.dto.VisualizationDto;

public final class VisualizationMapper {

    private VisualizationMapper() {
    }

    public static VisualizationDto toDto(Visualization v) {
        Long dreamId = v.getDreamRecord() != null ? v.getDreamRecord().getId() : null;
        return new VisualizationDto(
                v.getId(),
                v.getPrompt(),
                v.getGenerator(),
                v.getFilePath(),
                v.getMime(),
                v.getWidth(),
                v.getHeight(),
                v.getDuration(),
                v.getStatus(),
                dreamId,
                v.getCreatedAt()
        );
    }
}
