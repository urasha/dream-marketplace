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
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationRequestDto;
import ru.urasha.callmeani.dream_marketplace.dto.ImageGenerationResponseDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.ImageGenerationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    private final ImageGenerationService service;

    public ImageGenerationController(ImageGenerationService service) {
        this.service = service;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/generate")
    public ResponseEntity<ImageGenerationResponseDto> generate(@AuthenticationPrincipal JwtUserDetails user,
                                                               @Valid @RequestBody ImageGenerationRequestDto request) {
        return ResponseEntity.ok(service.create(user.userId(), request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ImageGenerationResponseDto> status(@PathVariable("id") UUID id) {
        ImageGenerationResponseDto dto = service.get(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
