package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingRequest(
        @Min(1) @Max(5) int value
) {
}
