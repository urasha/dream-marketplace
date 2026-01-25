package ru.urasha.callmeani.dream_marketplace.dto;

public record RatingSummaryDto(
        double average,
        long count,
        Integer userValue
) {
}
