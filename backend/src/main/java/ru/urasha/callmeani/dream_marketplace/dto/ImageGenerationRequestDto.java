package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageGenerationRequestDto {
    @NotBlank
    @Size(max = 1000)
    private String prompt;

    @Size(max = 32)
    private String modelVersion;

    @Size(max = 16)
    private String aspectRatio;

    private Boolean translateInput;

    private Boolean upgradePrompt;
}
