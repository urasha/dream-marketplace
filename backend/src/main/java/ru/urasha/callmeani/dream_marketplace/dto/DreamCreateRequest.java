package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;

public record DreamCreateRequest(
        @NotBlank String title,
        @NotBlank String content,
        @NotNull Privacy privacy,
        Long categoryId,
        List<Long> tagIds,
        List<String> tagNames
) {
}
