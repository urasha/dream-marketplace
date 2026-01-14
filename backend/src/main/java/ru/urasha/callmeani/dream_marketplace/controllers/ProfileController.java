package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.dto.UserDto;
import ru.urasha.callmeani.dream_marketplace.dto.ProfileUpdateRequest;
import ru.urasha.callmeani.dream_marketplace.mappers.UserMapper;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserAccountService userAccountService;

    public ProfileController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
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
}
