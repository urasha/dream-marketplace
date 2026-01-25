package ru.urasha.callmeani.dream_marketplace.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.urasha.callmeani.dream_marketplace.dto.CommentCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.CommentDto;
import ru.urasha.callmeani.dream_marketplace.dto.RatingRequest;
import ru.urasha.callmeani.dream_marketplace.dto.RatingSummaryDto;
import ru.urasha.callmeani.dream_marketplace.security.JwtUserDetails;
import ru.urasha.callmeani.dream_marketplace.service.LotFeedbackService;
import ru.urasha.callmeani.dream_marketplace.service.UserAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/lots/{lotId}")
public class LotFeedbackController {

    private final LotFeedbackService lotFeedbackService;
    private final UserAccountService userAccountService;

    public LotFeedbackController(LotFeedbackService lotFeedbackService, UserAccountService userAccountService) {
        this.lotFeedbackService = lotFeedbackService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> listComments(@PathVariable Long lotId) {
        return ResponseEntity.ok(lotFeedbackService.listComments(lotId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long lotId,
                                                 @Valid @RequestBody CommentCreateRequest request,
                                                 @AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        var dto = lotFeedbackService.addComment(lotId, user, request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/rating")
    public ResponseEntity<RatingSummaryDto> getRating(@PathVariable Long lotId,
                                                      @AuthenticationPrincipal JwtUserDetails details) {
        var user = details != null ? userAccountService.findById(details.userId()).orElse(null) : null;
        return ResponseEntity.ok(lotFeedbackService.getRating(lotId, user));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/rating")
    public ResponseEntity<RatingSummaryDto> setRating(@PathVariable Long lotId,
                                                      @Valid @RequestBody RatingRequest request,
                                                      @AuthenticationPrincipal JwtUserDetails details) {
        var user = userAccountService.findById(details.userId()).orElseThrow();
        return ResponseEntity.ok(lotFeedbackService.setRating(lotId, user, request.value()));
    }
}
