package ru.urasha.callmeani.dream_marketplace.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationCommandMessage {
    private UUID taskId;
    private Long userId;
    private String prompt;
    private String modelVersion;
    private String aspectRatio;
    private Boolean translateInput;
    private Boolean upgradePrompt;
}
