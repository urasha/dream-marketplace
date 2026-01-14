package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationRequestDto;
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationResponseDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.ImageGenerationTask;
import ru.urasha.callmeani.dream_marketplace.models.enums.ImageGenerationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.ImageGenerationTaskRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.ImageGenerationCommandMessage;

import java.util.UUID;

@Service
public class ImageGenerationService {

    private final ImageGenerationTaskRepository repository;
    private final ImageGenerationRequestProducer producer;

    public ImageGenerationService(ImageGenerationTaskRepository repository,
                                  ImageGenerationRequestProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    @Transactional
    public ImageGenerationResponseDto create(Long userId, ImageGenerationRequestDto request) {
        ImageGenerationTask task = ImageGenerationTask.builder()
                .userId(userId)
                .prompt(request.getPrompt())
                .modelVersion(request.getModelVersion())
                .aspectRatio(request.getAspectRatio())
                .status(ImageGenerationStatus.PENDING)
                .build();
        ImageGenerationTask saved = repository.save(task);

        ImageGenerationCommandMessage message = new ImageGenerationCommandMessage(
                saved.getId(),
                userId,
                request.getPrompt(),
                request.getModelVersion(),
                request.getAspectRatio(),
                request.getTranslateInput(),
                request.getUpgradePrompt()
        );
        producer.send(message);

        return ImageGenerationResponseDto.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public ImageGenerationResponseDto get(UUID id) {
        return repository.findById(id)
                .map(task -> ImageGenerationResponseDto.builder()
                        .id(task.getId())
                        .status(task.getStatus())
                        .resultUrl(task.getResultUrl())
                        .error(task.getError())
                        .build())
                .orElse(null);
    }
}
