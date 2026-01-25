package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;

import java.util.List;

@Repository
public interface VisualizationRepository extends JpaRepository<Visualization, Long> {
    List<Visualization> findByDreamRecordIdOrderByCreatedAtDesc(Long dreamRecordId);

    void deleteByDreamRecord_Id(Long dreamRecordId);
}
