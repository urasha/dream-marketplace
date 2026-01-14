package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.LotCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.LotDto;
import ru.urasha.callmeani.dream_marketplace.mappers.LotMapper;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.LotService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/lots")
public class LotController {

    private final LotService lotService;
    private final UserAccountService userAccountService;

    public LotController(LotService lotService, UserAccountService userAccountService) {
        this.lotService = lotService;
        this.userAccountService = userAccountService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<LotDto> create(@Valid @RequestBody LotCreateRequest request,
                                         @AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var lot = lotService.createLotFromVisualization(
                request.visualizationId(),
                user,
                request.title(),
                request.description(),
                request.price()
        );
        return ResponseEntity.ok(LotMapper.toDto(lot));
    }

    @GetMapping
    public ResponseEntity<List<LotDto>> listOpen() {
        var list = lotService.listOpen().stream().map(LotMapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mine")
    public ResponseEntity<List<LotDto>> listOwn(@AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var list = lotService.listOwn(user).stream().map(LotMapper::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotDto> get(@PathVariable Long id, @AuthenticationPrincipal JwtUserDetails details) {
        var user = details == null ? null : userAccountService.findById(details.userId()).orElse(null);
        var lot = lotService.getLotForPublic(id, user);
        return ResponseEntity.ok(LotMapper.toDto(lot));
    }
}
