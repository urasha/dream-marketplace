package ru.urasha.callmeani.dream_marketplace.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.DreamService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.web.dto.VisualizationDto;
import ru.urasha.callmeani.dream_marketplace.web.mapper.VisualizationMapper;

@RestController
@RequestMapping("/api/visualizations")
public class VisualizationController {

    private final DreamService dreamService;
    private final UserAccountService userAccountService;

    public VisualizationController(DreamService dreamService, UserAccountService userAccountService) {
        this.dreamService = dreamService;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<VisualizationDto> accept(@PathVariable Long id, Authentication authentication) {
        UserAccount user = requireUser(authentication);
        var vis = dreamService.acceptVisualization(id, user);
        return ResponseEntity.ok(VisualizationMapper.toDto(vis));
    }

    private UserAccount requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserDetails details)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return userAccountService.findById(details.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
