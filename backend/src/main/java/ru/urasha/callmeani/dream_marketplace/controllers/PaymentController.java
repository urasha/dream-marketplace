package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.dto.DepositCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.PaymentDto;
import ru.urasha.callmeani.dream_marketplace.dto.WalletDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.PaymentService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserAccountService userAccountService;

    public PaymentController(PaymentService paymentService, UserAccountService userAccountService) {
        this.paymentService = paymentService;
        this.userAccountService = userAccountService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/deposit")
    public ResponseEntity<PaymentDto> deposit(@AuthenticationPrincipal JwtUserDetails details,
                                              @Valid @RequestBody DepositCreateRequest request) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        return ResponseEntity.ok(paymentService.createDeposit(user, request.amount()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> status(@AuthenticationPrincipal JwtUserDetails details,
                                             @PathVariable("id") UUID id) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        return ResponseEntity.ok(paymentService.refreshAndGet(user, id));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/wallet")
    public ResponseEntity<WalletDto> wallet(@AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(new WalletDto(paymentService.getBalance(details.userId())));
    }
}
