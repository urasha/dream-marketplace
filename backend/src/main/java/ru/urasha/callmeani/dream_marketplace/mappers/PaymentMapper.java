package ru.urasha.callmeani.dream_marketplace.mappers;

import ru.urasha.callmeani.dream_marketplace.dto.PaymentDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.PaymentIntent;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentDto toDto(PaymentIntent intent) {
        return new PaymentDto(
                intent.getId(),
                intent.getAmount(),
                intent.getStatus(),
                intent.getConfirmationUrl(),
                intent.isCredited(),
                intent.getCreatedAt(),
                intent.getUpdatedAt()
        );
    }
}
