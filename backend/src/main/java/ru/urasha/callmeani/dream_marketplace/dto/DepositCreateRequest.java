package ru.urasha.callmeani.dream_marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DepositCreateRequest(
        @NotNull @DecimalMin(value = "1.00", message = "Минимальная сумма 1.00") BigDecimal amount
) {}
