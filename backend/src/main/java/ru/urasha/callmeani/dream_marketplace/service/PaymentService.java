package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.PaymentDto;
import ru.urasha.callmeani.dream_marketplace.mappers.PaymentMapper;
import ru.urasha.callmeani.dream_marketplace.models.entities.PaymentIntent;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.PaymentStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.PaymentIntentRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.UserAccountRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.YooKassaPaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentIntentRepository paymentIntentRepository;
    private final UserAccountRepository userAccountRepository;
    private final YooKassaClient yooKassaClient;

    public PaymentService(PaymentIntentRepository paymentIntentRepository,
                          UserAccountRepository userAccountRepository,
                          YooKassaClient yooKassaClient) {
        this.paymentIntentRepository = paymentIntentRepository;
        this.userAccountRepository = userAccountRepository;
        this.yooKassaClient = yooKassaClient;
    }

    @Transactional
    public PaymentDto createDeposit(UserAccount user, BigDecimal amount) {
        if (amount == null || amount.compareTo(new BigDecimal("1.00")) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Минимальная сумма 1.00");
        }
        YooKassaPaymentResponse response = yooKassaClient.createPayment(amount, "Пополнение баланса Dream Marketplace");
        if (response.id() == null || response.confirmationUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Не удалось создать платеж в YooKassa");
        }

        PaymentIntent intent = new PaymentIntent();
        intent.setUser(user);
        intent.setAmount(amount);
        intent.setProviderPaymentId(response.id());
        intent.setConfirmationUrl(response.confirmationUrl());
        intent.setStatus(PaymentStatus.PENDING);
        intent.setCredited(false);
        paymentIntentRepository.save(intent);
        return PaymentMapper.toDto(intent);
    }

    @Transactional
    public PaymentDto refreshAndGet(UserAccount user, UUID intentId) {
        PaymentIntent intent = paymentIntentRepository.findById(intentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Платеж не найден"));
        if (!intent.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Чужой платеж");
        }

        if (intent.getStatus() == PaymentStatus.SUCCEEDED || intent.getStatus() == PaymentStatus.CANCELED || intent.getStatus() == PaymentStatus.FAILED) {
            return PaymentMapper.toDto(intent);
        }

        YooKassaPaymentResponse response = yooKassaClient.getPayment(intent.getProviderPaymentId());
        String status = response.status();
        if ("succeeded".equalsIgnoreCase(status) || response.paid()) {
            markSucceeded(intent);
        } else if ("canceled".equalsIgnoreCase(status)) {
            intent.setStatus(PaymentStatus.CANCELED);
        } else if ("waiting_for_capture".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)) {
            intent.setStatus(PaymentStatus.PENDING);
        } else {
            intent.setStatus(PaymentStatus.FAILED);
        }
        return PaymentMapper.toDto(intent);
    }

    private void markSucceeded(PaymentIntent intent) {
        if (intent.isCredited()) {
            intent.setStatus(PaymentStatus.SUCCEEDED);
            return;
        }
        UserAccount user = intent.getUser();
        UserAccount managedUser = userAccountRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        managedUser.setBalance(managedUser.getBalance().add(intent.getAmount()));
        userAccountRepository.save(managedUser);

        intent.setCredited(true);
        intent.setStatus(PaymentStatus.SUCCEEDED);
        paymentIntentRepository.save(intent);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        return userAccountRepository.findById(userId)
                .map(UserAccount::getBalance)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }
}
