package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationRequestDto;
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationResponseDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.ImageGenerationTask;
import ru.urasha.callmeani.dream_marketplace.models.enums.ImageGenerationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.ImageGenerationTaskRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.ImageGenerationCommandMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

@Service
public class ImageGenerationService {

    private final ImageGenerationTaskRepository repository;
    private final ImageGenerationRequestProducer producer;
        private final ObjectMapper objectMapper;

        public ImageGenerationService(ImageGenerationTaskRepository repository,
                                                                  ImageGenerationRequestProducer producer,
                                                                  ObjectMapper objectMapper) {
        this.repository = repository;
        this.producer = producer;
                this.objectMapper = objectMapper;
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
                                                .resultUrl(primaryUrl(task))
                                                .resultUrls(parseResultUrls(task))
                        .error(task.getError())
                        .build())
                .orElse(null);
    }

        private List<String> parseResultUrls(ImageGenerationTask task) {
                String raw = task.getResultUrls();
                if (raw == null || raw.isBlank()) {
                        return task.getResultUrl() != null ? List.of(task.getResultUrl()) : Collections.emptyList();
                }
                try {
                        List<String> urls = objectMapper.readValue(raw, new TypeReference<List<String>>() {});
                        return urls == null ? Collections.emptyList() : urls;
                } catch (Exception e) {
                        return List.of(raw);
                }
        }

        private String primaryUrl(ImageGenerationTask task) {
                if (task.getResultUrl() != null && !task.getResultUrl().isBlank()) {
                        return task.getResultUrl();
                }
                List<String> urls = parseResultUrls(task);
                return urls.isEmpty() ? null : urls.get(0);
        }
}
