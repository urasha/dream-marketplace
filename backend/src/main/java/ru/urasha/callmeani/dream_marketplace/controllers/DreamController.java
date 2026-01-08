package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.service.DreamService;
import ru.urasha.callmeani.dream_marketplace.dto.DreamCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.DreamDto;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationDto;
import ru.urasha.callmeani.dream_marketplace.mappers.DreamMapper;
import ru.urasha.callmeani.dream_marketplace.mappers.VisualizationMapper;

import java.util.List;

@RestController
@RequestMapping("/api/dreams")
public class DreamController {

    private final DreamService dreamService;
    private final UserAccountService userAccountService;

    public DreamController(DreamService dreamService, UserAccountService userAccountService) {
        this.dreamService = dreamService;
        this.userAccountService = userAccountService;
    }

    @PostMapping
    public ResponseEntity<DreamDto> create(@Valid @RequestBody DreamCreateRequest request,
                                           Authentication authentication) {
        UserAccount user = requireUser(authentication);
        var dream = dreamService.create(user, request.title(), request.content(), request.privacy(), request.categoryId(), request.tagIds());
        return ResponseEntity.ok(DreamMapper.toDto(dream));
    }

    @GetMapping
    public ResponseEntity<List<DreamDto>> myDreams(Authentication authentication) {
        UserAccount user = requireUser(authentication);
        var dreams = dreamService.findOwn(user).stream()
                .map(DreamMapper::toDto)
                .toList();
        return ResponseEntity.ok(dreams);
    }

    @PostMapping("/{id}/visualize")
    public ResponseEntity<VisualizationDto> visualize(@PathVariable Long id, Authentication authentication) {
        UserAccount user = requireUser(authentication);
        var vis = dreamService.requestVisualization(id, user);
        return ResponseEntity.accepted().body(VisualizationMapper.toDto(vis));
    }

    @GetMapping("/{id}/visualizations")
    public ResponseEntity<List<VisualizationDto>> visualizations(@PathVariable Long id, Authentication authentication) {
        UserAccount user = requireUser(authentication);
        var list = dreamService.getVisualizations(id, user).stream()
                .map(VisualizationMapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    private UserAccount requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserDetails details)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return userAccountService.findById(details.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
