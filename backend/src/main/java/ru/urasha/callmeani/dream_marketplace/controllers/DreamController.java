package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;
import ru.urasha.callmeani.dream_marketplace.service.DreamService;
import ru.urasha.callmeani.dream_marketplace.dto.DreamCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.DreamDto;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationDto;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationUploadRequest;
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<DreamDto> create(@Valid @RequestBody DreamCreateRequest request,
                                           @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var dream = dreamService.create(user, request.title(), request.content(), request.privacy(), request.categoryId(), request.tagIds());
        return ResponseEntity.ok(DreamMapper.toDto(dream));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<DreamDto>> myDreams(@AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var dreams = dreamService.findOwn(user).stream()
                .map(DreamMapper::toDto)
                .toList();
        return ResponseEntity.ok(dreams);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/visualize")
    public ResponseEntity<VisualizationDto> visualize(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var vis = dreamService.requestVisualization(id, user);
        return ResponseEntity.accepted().body(VisualizationMapper.toDto(vis));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/visualizations")
    public ResponseEntity<List<VisualizationDto>> visualizations(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var list = dreamService.getVisualizations(id, user).stream()
                .map(VisualizationMapper::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/visualizations/attach")
    public ResponseEntity<VisualizationDto> attachVisualization(@PathVariable Long id,
                                                                @RequestBody VisualizationUploadRequest request,
                                                                @AuthenticationPrincipal JwtUserDetails details) {
        UserAccount user = requireUser(details);
        var vis = dreamService.attachReadyVisualization(id, user, request);
        return ResponseEntity.ok(VisualizationMapper.toDto(vis));
    }

    private UserAccount requireUser(JwtUserDetails details) {
        return userAccountService.findById(details.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
