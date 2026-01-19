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
import ru.urasha.callmeani.dream_marketplace.dto.ModerationDecisionRequest;
import ru.urasha.callmeani.dream_marketplace.dto.ModerationLogDto;
import ru.urasha.callmeani.dream_marketplace.dto.ModerationQueueItemDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.ModerationService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final ModerationService moderationService;
    private final UserAccountService userAccountService;

    public AdminModerationController(ModerationService moderationService, UserAccountService userAccountService) {
        this.moderationService = moderationService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/lots/pending")
    public ResponseEntity<List<ModerationQueueItemDto>> pendingLots() {
        return ResponseEntity.ok(moderationService.listPendingLots());
    }

    @GetMapping("/moderation-log")
    public ResponseEntity<List<ModerationLogDto>> moderationLog() {
        return ResponseEntity.ok(moderationService.listLog());
    }

    @PostMapping("/lots/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id,
                                        @AuthenticationPrincipal JwtUserDetails details) {
        var admin = userAccountService.findById(details.userId()).orElseThrow();
        moderationService.approve(id, admin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/lots/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id,
                                       @Valid @RequestBody ModerationDecisionRequest request,
                                       @AuthenticationPrincipal JwtUserDetails details) {
        var admin = userAccountService.findById(details.userId()).orElseThrow();
        moderationService.reject(id, admin, request.reason());
        return ResponseEntity.noContent().build();
    }
}