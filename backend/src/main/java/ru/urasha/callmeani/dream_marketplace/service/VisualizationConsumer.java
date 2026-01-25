package ru.urasha.callmeani.dream_marketplace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.callmeani.dream_marketplace.config.VisualizationProperties;
import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;
import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.VisualizationRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.VisualizationRequestMessage;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class VisualizationConsumer {

    private static final Logger log = LoggerFactory.getLogger(VisualizationConsumer.class);

    private final ObjectMapper objectMapper;
    private final VisualizationRepository visualizationRepository;
    private final S3StorageService storageService;
    private final VisualizationProperties visualizationProperties;
    private final String bucketKeyPrefix = "dreams";

    public VisualizationConsumer(ObjectMapper objectMapper,
                                 VisualizationRepository visualizationRepository,
                                 S3StorageService storageService,
                                 VisualizationProperties visualizationProperties) {
        this.objectMapper = objectMapper;
        this.visualizationRepository = visualizationRepository;
        this.storageService = storageService;
        this.visualizationProperties = visualizationProperties;
    }

    @KafkaListener(topics = "${app.kafka.visualization-topic}", groupId = "visualization-workers")
    @Transactional
    public void consume(ConsumerRecord<String, String> record) {
        VisualizationRequestMessage msg = parse(record.value());
        Optional<Visualization> opt = visualizationRepository.findById(msg.visualizationId());
        if (opt.isEmpty()) {
            log.warn("Visualization not found for id={} from kafka", msg.visualizationId());
            return;
        }
        Visualization vis = opt.get();
        try {
            if (visualizationProperties.isMockEnabled()) {
                vis.setFilePath(visualizationProperties.getMockImageUrl());
                vis.setMime("image/png");
                vis.setStatus(VisualizationStatus.READY);
            } else {
                // Simulate generation and upload placeholder bytes (until real image pipeline is connected)
                String key = bucketKeyPrefix + "/" + msg.dreamId() + "/visualization-" + msg.visualizationId() + ".txt";
                String mockContent = "Generated visualization for prompt: " + msg.prompt();
                String uri = storageService.uploadBytes(key, mockContent.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");

                vis.setFilePath(uri);
                vis.setMime("text/plain");
                vis.setStatus(VisualizationStatus.READY);
            }
        } catch (Exception e) {
            vis.setStatus(VisualizationStatus.FAILED);
            log.error("Failed to process visualization {}", msg.visualizationId(), e);
        }
    }

    private VisualizationRequestMessage parse(String json) {
        try {
            return objectMapper.readValue(json, VisualizationRequestMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse visualization message", e);
        }
    }
}
