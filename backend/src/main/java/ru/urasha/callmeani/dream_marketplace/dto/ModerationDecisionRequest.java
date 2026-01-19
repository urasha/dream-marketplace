package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.NotBlank;

public record ModerationDecisionRequest(
        @NotBlank String reason
) {
}