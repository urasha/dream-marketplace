package ru.urasha.callmeani.dream_marketplace.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.urasha.callmeani.dream_marketplace.service.dto.VisualizationRequestMessage;

@Service
public class VisualizationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public VisualizationProducer(KafkaTemplate<String, String> kafkaTemplate,
                                 ObjectMapper objectMapper,
                                 @Value("${app.kafka.visualization-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void send(VisualizationRequestMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize visualization message", e);
        }
    }
}
