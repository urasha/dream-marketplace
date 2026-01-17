package ru.urasha.callmeani.dream_marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import ru.urasha.callmeani.dream_marketplace.models.entities.ImageGenerationTask;
import ru.urasha.callmeani.dream_marketplace.models.enums.ImageGenerationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.ImageGenerationTaskRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiCreateResponse;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiMidjourneyRequest;
import ru.urasha.callmeani.dream_marketplace.service.dto.GenApiStatusResponse;
import ru.urasha.callmeani.dream_marketplace.service.dto.ImageGenerationCommandMessage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ImageGenerationWorker {

    private static final Logger log = LoggerFactory.getLogger(ImageGenerationWorker.class);
    // Allow more time for slow external generation
    private static final Duration MAX_WAIT = Duration.ofSeconds(300);
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;
    private final ImageGenerationTaskRepository repository;
    private final GenApiClient genApiClient;
    private final RestTemplate restTemplate;
    private final S3StorageService storageService;
    private final String s3Prefix = "generated/images";

    public ImageGenerationWorker(ObjectMapper objectMapper,
                                 ImageGenerationTaskRepository repository,
                                 GenApiClient genApiClient,
                                 RestTemplate restTemplate,
                                 S3StorageService storageService) {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.genApiClient = genApiClient;
        this.restTemplate = restTemplate;
        this.storageService = storageService;
    }

    @KafkaListener(topics = "${app.kafka.image-generation-topic}", groupId = "image-generation-workers")
    public void consume(ConsumerRecord<String, String> record) {
        ImageGenerationCommandMessage cmd = parse(record.value());
        Optional<ImageGenerationTask> opt = repository.findById(cmd.getTaskId());
        if (opt.isEmpty()) {
            log.warn("Generation task not found id={} from kafka", cmd.getTaskId());
            return;
        }
        ImageGenerationTask task = opt.get();
        task.setStatus(ImageGenerationStatus.PROCESSING);
        repository.save(task);

        try {
            GenApiMidjourneyRequest request = buildMidjourneyRequest(cmd);
            GenApiCreateResponse createResp = genApiClient.createMidjourneyTask(request);
            task.setProviderRequestId(createResp.getRequestId());

            GenApiStatusResponse statusResp = pollStatus(createResp.getRequestId());
            if (statusResp == null) {
                fail(task, "Generation timed out");
                return;
            }
            if (!"success".equalsIgnoreCase(statusResp.getStatus())) {
                String err = statusResp.getResult() != null ? statusResp.getResult().toString() : "Generation failed";
                fail(task, err);
                return;
            }

            List<String> imageUrls = extractImageUrls(statusResp);
            if (imageUrls.isEmpty()) {
                fail(task, "Generation finished without image URL");
                return;
            }

            List<String> stored = new ArrayList<>();
            int idx = 0;
            for (String src : imageUrls) {
                byte[] data = fetchImage(src);
                String key = s3Prefix + "/" + task.getId() + "-" + idx + ".png";
                String storedUrl = storageService.uploadBytes(key, data, "image/png");
                stored.add(storedUrl);
                idx++;
            }

            if (!stored.isEmpty()) {
                task.setResultUrl(stored.get(0));
                task.setResultUrls(objectMapper.writeValueAsString(stored));
            }
            task.setStatus(ImageGenerationStatus.DONE);
            repository.save(task);
        } catch (Exception e) {
            log.error("Failed to process image generation task {}", task.getId(), e);
            fail(task, e.getMessage());
        }
    }

    private ImageGenerationCommandMessage parse(String json) {
        try {
            return objectMapper.readValue(json, ImageGenerationCommandMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse image generation message", e);
        }
    }

    private GenApiMidjourneyRequest buildMidjourneyRequest(ImageGenerationCommandMessage cmd) {
        return GenApiMidjourneyRequest.builder()
                .prompt(cmd.getPrompt())
                .modelString(cmd.getModelVersion() != null ? cmd.getModelVersion() : "7.0")
                .aspectRatio(cmd.getAspectRatio() != null ? cmd.getAspectRatio() : "1:1")
                .chaos(30)
                .quality("1")
                .stop(100)
                .stylize(1000)
                .tile(false)
                .weird(0)
                .translateInput(cmd.getTranslateInput() != null ? cmd.getTranslateInput() : Boolean.TRUE)
                .upgradePrompt(cmd.getUpgradePrompt() != null ? cmd.getUpgradePrompt() : Boolean.FALSE)
                .callbackUrl(null)
                .build();
    }

    private GenApiStatusResponse pollStatus(String requestId) throws InterruptedException {
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).compareTo(MAX_WAIT) < 0) {
            GenApiStatusResponse status = genApiClient.getStatus(requestId);
            if (status == null) {
                return null;
            }
            if ("success".equalsIgnoreCase(status.getStatus())) {
                return status;
            }
            if ("failed".equalsIgnoreCase(status.getStatus()) || "error".equalsIgnoreCase(status.getStatus())) {
                return status;
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
        return null;
    }

    private List<String> extractImageUrls(GenApiStatusResponse statusResp) {
        List<String> urls = extractFromNode(statusResp.getOutput());
        if (urls.isEmpty()) {
            urls = extractFromNode(statusResp.getResult());
        }
        return urls;
    }

    private List<String> extractFromNode(JsonNode node) {
        if (node == null) {
            return Collections.emptyList();
        }
        List<String> urls = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> {
                if (n.isTextual()) {
                    urls.add(n.asText());
                } else if (n.has("url")) {
                    urls.add(n.get("url").asText());
                }
            });
        } else if (node.isTextual()) {
            urls.add(node.asText());
        } else if (node.has("url")) {
            urls.add(node.get("url").asText());
        }
        return urls;
    }

    private byte[] fetchImage(String url) {
        try {
            ResponseEntity<byte[]> resp = restTemplate.getForEntity(url, byte[].class);
            if (resp.getBody() != null) {
                return resp.getBody();
            }
        } catch (Exception e) {
            log.warn("Failed to download image from {}, fallback to placeholder", url, e);
        }
        return ("failed to download " + url).getBytes(StandardCharsets.UTF_8);
    }

    private void fail(ImageGenerationTask task, String message) {
        task.setStatus(ImageGenerationStatus.FAILED);
        task.setError(message);
        repository.save(task);
    }
}
