package ru.urasha.callmeani.dream_marketplace.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.urasha.callmeani.dream_marketplace.models.enums.ImageGenerationStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "image_generation_tasks")
public class ImageGenerationTask {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "text", nullable = false)
    private String prompt;

    @Column(name = "model_version", length = 32)
    private String modelVersion;

    @Column(name = "aspect_ratio", length = 16)
    private String aspectRatio;

    @Column(name = "provider_request_id", length = 64)
    private String providerRequestId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private ImageGenerationStatus status;

    @Column(name = "result_url", columnDefinition = "text")
    private String resultUrl;

    @Column(columnDefinition = "text")
    private String error;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
