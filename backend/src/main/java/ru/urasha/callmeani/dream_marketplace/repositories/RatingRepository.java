package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.Rating;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByLot_IdAndUser_Id(Long lotId, Long userId);

    @Query("select coalesce(avg(r.value), 0) from Rating r where r.lot.id = :lotId")
    double averageForLot(@Param("lotId") Long lotId);

    @Query("select count(r) from Rating r where r.lot.id = :lotId")
    long countForLot(@Param("lotId") Long lotId);
}
