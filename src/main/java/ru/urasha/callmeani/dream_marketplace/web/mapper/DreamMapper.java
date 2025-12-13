package ru.urasha.callmeani.dream_marketplace.web.mapper;

import ru.urasha.callmeani.dream_marketplace.models.entities.DreamRecord;
import ru.urasha.callmeani.dream_marketplace.web.dto.DreamDto;

import java.util.Set;
import java.util.stream.Collectors;

public final class DreamMapper {

    private DreamMapper() {
    }

    public static DreamDto toDto(DreamRecord dream) {
        Long categoryId = dream.getCategory() != null ? dream.getCategory().getId() : null;
        Long visualizationId = dream.getVisualization() != null ? dream.getVisualization().getId() : null;
        Set<Long> tagIds = dream.getTags() == null ? Set.of() : dream.getTags().stream()
                .map(tag -> tag.getId())
                .collect(Collectors.toSet());
        return new DreamDto(
                dream.getId(),
                dream.getTitle(),
                dream.getContent(),
                dream.getPrivacy(),
                categoryId,
                tagIds,
                visualizationId,
                dream.getCreatedAt(),
                dream.getUpdatedAt()
        );
    }
}
