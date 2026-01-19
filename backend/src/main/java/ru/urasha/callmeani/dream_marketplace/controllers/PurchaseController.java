package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.dto.TransactionDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.PurchaseService;

@RestController
@RequestMapping("/api/lots")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/buy")
    public ResponseEntity<TransactionDto> buy(@PathVariable("id") Long id,
                                              @AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(purchaseService.buyLot(id, details.userId()));
    }
}
