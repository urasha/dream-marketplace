package ru.urasha.callmeani.dream_marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(
        Long id,
        Long lotId,
        Long buyerId,
        Long sellerId,
        BigDecimal amount,
        BigDecimal fee,
        LocalDateTime transactionDate
) {}
