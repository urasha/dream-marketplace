package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.dto.NotificationDto;
import ru.urasha.callmeani.dream_marketplace.dto.NotificationUnreadDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(@AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(notificationService.listForUser(details.userId()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadDto> unreadCount(@AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(new NotificationUnreadDto(notificationService.countUnread(details.userId())));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markRead(@PathVariable Long id,
                                                    @AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(notificationService.markRead(details.userId(), id));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal JwtUserDetails details) {
        notificationService.markAllRead(details.userId());
        return ResponseEntity.ok().build();
    }
}
