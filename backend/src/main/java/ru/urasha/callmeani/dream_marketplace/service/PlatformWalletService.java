package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.PlatformWallet;
import ru.urasha.callmeani.dream_marketplace.repositories.PlatformWalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PlatformWalletService {

    private static final long PLATFORM_WALLET_ID = 1L;

    private final PlatformWalletRepository repository;

    public PlatformWalletService(PlatformWalletRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void addCommission(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        PlatformWallet wallet = repository.findById(PLATFORM_WALLET_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Платформенный кошелёк не найден"));

        BigDecimal next = wallet.getBalance()
                .add(amount)
                .setScale(2, RoundingMode.HALF_UP);
        wallet.setBalance(next);
        repository.save(wallet);
    }
}