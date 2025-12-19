package ru.urasha.callmeani.dream_marketplace.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "lot")
public class Lot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_record_id")
    private DreamRecord dreamRecord;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LotStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "moderation_reason", columnDefinition = "TEXT")
    private String moderationReason;
}