package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.PlatformWallet;

@Repository
public interface PlatformWalletRepository extends JpaRepository<PlatformWallet, Long> {
}