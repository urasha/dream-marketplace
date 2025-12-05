package ru.urasha.callmeani.dream_marketplace.repositories;

import ru.urasha.callmeani.dream_marketplace.models.entities.DreamRecord;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DreamRepository extends JpaRepository<DreamRecord, Long> {

    List<DreamRecord> findByUserOrderByCreatedAtDesc(UserAccount user);

    List<DreamRecord> findByPrivacyOrderByCreatedAtDesc(Privacy privacy);

    Optional<DreamRecord> findByIdAndUser(Long id, UserAccount user);

    List<DreamRecord> findByCategoryIdOrderByCreatedAtDesc(Integer categoryId);

    @Query("SELECT d FROM DreamRecord d JOIN d.tags t WHERE t.name = :tagName AND d.privacy = 'PUBLIC' ORDER BY d.createdAt DESC")
    List<DreamRecord> findPublicDreamsByTag(@Param("tagName") String tagName);
}