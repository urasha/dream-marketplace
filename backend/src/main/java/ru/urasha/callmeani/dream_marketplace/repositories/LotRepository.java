package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    @Modifying
    @Transactional
    @Query(value = "CALL proc_archive_lot(:lotId, :adminId, :reason)", nativeQuery = true)
    void archiveLot(@Param("lotId") Long lotId,
                    @Param("adminId") Long adminId,
                    @Param("reason") String reason);
}