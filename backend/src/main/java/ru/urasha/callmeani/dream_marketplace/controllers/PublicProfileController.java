package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.DreamDto;
import ru.urasha.callmeani.dream_marketplace.dto.LotDto;
import ru.urasha.callmeani.dream_marketplace.dto.PublicUserDto;
import ru.urasha.callmeani.dream_marketplace.dto.PurchaseItemDto;
import ru.urasha.callmeani.dream_marketplace.mappers.DreamMapper;
import ru.urasha.callmeani.dream_marketplace.mappers.LotMapper;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;
import ru.urasha.callmeani.dream_marketplace.repositories.DreamRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.RatingRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.UserAccountRepository;
import ru.urasha.callmeani.dream_marketplace.service.PurchaseService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final UserAccountRepository userAccountRepository;
    private final LotRepository lotRepository;
    private final RatingRepository ratingRepository;
    private final DreamRepository dreamRepository;
    private final PurchaseService purchaseService;

    public PublicProfileController(UserAccountRepository userAccountRepository,
                                   LotRepository lotRepository,
                                   RatingRepository ratingRepository,
                                   DreamRepository dreamRepository,
                                   PurchaseService purchaseService) {
        this.userAccountRepository = userAccountRepository;
        this.lotRepository = lotRepository;
        this.ratingRepository = ratingRepository;
        this.dreamRepository = dreamRepository;
        this.purchaseService = purchaseService;
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicUserDto> profile(@PathVariable Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(new PublicUserDto(user.getId(), user.getUsername()));
    }

    @GetMapping("/{id}/lots")
    public ResponseEntity<List<LotDto>> lots(@PathVariable Long id) {
        List<LotDto> lots = lotRepository.findByDreamRecord_User_IdOrderBySubmittedAtDesc(id).stream()
                .map(lot -> LotMapper.toDto(
                        lot,
                        ratingRepository.averageForLot(lot.getId()),
                        ratingRepository.countForLot(lot.getId())
                ))
                .toList();
        return ResponseEntity.ok(lots);
    }

    @GetMapping("/{id}/dreams")
    public ResponseEntity<List<DreamDto>> dreams(@PathVariable Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<DreamDto> dreams = dreamRepository.findByUserAndPrivacyOrderByCreatedAtDesc(user, Privacy.PUBLIC).stream()
                .map(dream -> DreamMapper.toDto(dream, lotRepository.existsByDreamRecordId(dream.getId())))
                .toList();
        return ResponseEntity.ok(dreams);
    }

    @GetMapping("/{id}/purchases")
    public ResponseEntity<List<PurchaseItemDto>> purchases(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.listPurchases(id));
    }
}
