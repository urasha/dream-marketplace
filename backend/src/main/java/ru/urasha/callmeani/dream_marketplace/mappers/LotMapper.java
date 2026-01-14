package ru.urasha.callmeani.dream_marketplace.mappers;

import ru.urasha.callmeani.dream_marketplace.dto.LotDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class LotMapper {
    private LotMapper() {
    }

    public static LotDto toDto(Lot lot) {
        var dream = lot.getDreamRecord();
        var user = dream != null ? dream.getUser() : null;
        var category = dream != null ? dream.getCategory() : null;
        List<String> tags = dream != null && dream.getTags() != null
            ? dream.getTags().stream().map(t -> t.getName()).collect(Collectors.toList())
            : Collections.emptyList();

        return new LotDto(
                lot.getId(),
            dream != null ? dream.getId() : null,
            dream != null && dream.getVisualization() != null
                ? dream.getVisualization().getId()
                        : null,
                lot.getTitle(),
                lot.getDescription(),
                lot.getPrice(),
                lot.getStatus(),
            user != null ? user.getId() : null,
            user != null ? user.getUsername() : null,
            category != null ? category.getId() : null,
            category != null ? category.getName() : null,
            tags,
                lot.getSubmittedAt(),
                lot.getReviewedAt(),
                lot.getModerationReason()
        );
    }
}
