package ru.urasha.callmeani.dream_marketplace.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "dream_record")
public class DreamRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visualization_id")
    private Visualization visualization;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "dream_record_tag",
            joinColumns = @JoinColumn(name = "dream_record_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}