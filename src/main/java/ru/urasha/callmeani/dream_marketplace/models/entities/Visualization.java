package ru.urasha.callmeani.dream_marketplace.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visualization")
public class Visualization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prompt;
    private String generator;

    @Column(name = "file_path")
    private String filePath;

    private String mime;
    private Integer width;
    private Integer height;
    private Integer duration;

    @Enumerated(EnumType.STRING)
    private VisualizationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}