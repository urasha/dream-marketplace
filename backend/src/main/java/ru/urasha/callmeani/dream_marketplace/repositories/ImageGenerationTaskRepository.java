package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.urasha.callmeani.dream_marketplace.models.entities.ImageGenerationTask;

import java.util.UUID;

public interface ImageGenerationTaskRepository extends JpaRepository<ImageGenerationTask, UUID> {
}
