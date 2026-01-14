package ru.urasha.callmeani.dream_marketplace.mappers;

import ru.urasha.callmeani.dream_marketplace.dto.LotDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;

public final class LotMapper {
    private LotMapper() {
    }

    public static LotDto toDto(Lot lot) {
        return new LotDto(
                lot.getId(),
                lot.getDreamRecord() != null ? lot.getDreamRecord().getId() : null,
                lot.getDreamRecord() != null && lot.getDreamRecord().getVisualization() != null
                        ? lot.getDreamRecord().getVisualization().getId()
                        : null,
                lot.getTitle(),
                lot.getDescription(),
                lot.getPrice(),
                lot.getStatus(),
            lot.getDreamRecord() != null && lot.getDreamRecord().getUser() != null
                ? lot.getDreamRecord().getUser().getUsername()
                : null,
                lot.getSubmittedAt(),
                lot.getReviewedAt(),
                lot.getModerationReason()
        );
    }
}
