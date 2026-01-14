package ru.urasha.callmeani.dream_marketplace.dto;

import lombok.Builder;
import lombok.Value;
import ru.urasha.callmeani.dream_marketplace.models.enums.ImageGenerationStatus;

import java.util.UUID;

@Value
@Builder
public class ImageGenerationResponseDto {
    UUID id;
    ImageGenerationStatus status;
    String resultUrl;
    String error;
}
