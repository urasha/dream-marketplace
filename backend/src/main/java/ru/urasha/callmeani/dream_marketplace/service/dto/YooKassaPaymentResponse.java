package ru.urasha.callmeani.dream_marketplace.service.dto;

public record YooKassaPaymentResponse(
        String id,
        String status,
        boolean paid,
        String confirmationUrl
) {}
