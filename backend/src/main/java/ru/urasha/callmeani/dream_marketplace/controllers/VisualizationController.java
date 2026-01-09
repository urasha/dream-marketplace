package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.DreamService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationDto;
import ru.urasha.callmeani.dream_marketplace.mappers.VisualizationMapper;

@RestController
@RequestMapping("/api/visualizations")
public class VisualizationController {

    private final DreamService dreamService;
    private final UserAccountService userAccountService;

    public VisualizationController(DreamService dreamService, UserAccountService userAccountService) {
        this.dreamService = dreamService;
        this.userAccountService = userAccountService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/accept")
    public ResponseEntity<VisualizationDto> accept(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var vis = dreamService.acceptVisualization(id, user);
        return ResponseEntity.ok(VisualizationMapper.toDto(vis));
    }

    private UserAccount requireUser(JwtUserDetails details) {
        return userAccountService.findById(details.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
