package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.dto.UserDto;
import ru.urasha.callmeani.dream_marketplace.dto.ProfileUpdateRequest;
import ru.urasha.callmeani.dream_marketplace.mappers.UserMapper;
import ru.urasha.callmeani.dream_marketplace.dto.PurchaseItemDto;
import ru.urasha.callmeani.dream_marketplace.service.PurchaseService;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserAccountService userAccountService;
    private final PurchaseService purchaseService;

    public ProfileController(UserAccountService userAccountService, PurchaseService purchaseService) {
        this.userAccountService = userAccountService;
        this.purchaseService = purchaseService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal JwtUserDetails details) {
        return userAccountService.findById(details.userId())
                .map(UserMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateMe(@AuthenticationPrincipal JwtUserDetails details,
                                            @Valid @RequestBody ProfileUpdateRequest request) {
        boolean hasUsername = request.username() != null && !request.username().isBlank();
        boolean hasEmail = request.email() != null && !request.email().isBlank();

        if (!hasUsername && !hasEmail) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
                UserMapper.toDto(
                        userAccountService.updateProfile(details.userId(), request.username(), request.email())
                )
        );
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/avatar")
    public ResponseEntity<UserDto> updateAvatar(@AuthenticationPrincipal JwtUserDetails details,
                                                @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(UserMapper.toDto(userAccountService.updateAvatar(details.userId(), file)));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/purchases")
    public ResponseEntity<List<PurchaseItemDto>> purchases(@AuthenticationPrincipal JwtUserDetails details) {
        return ResponseEntity.ok(purchaseService.listPurchases(details.userId()));
    }
}
