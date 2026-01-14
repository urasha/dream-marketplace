package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record LotCreateRequest(
        @NotNull Long visualizationId,
        @NotBlank String title,
        String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        Long categoryId,
        List<Long> tagIds,
        List<String> tagNames
) {
}
