package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.ModerationLogDto;
import ru.urasha.callmeani.dream_marketplace.dto.ModerationQueueItemDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;
import ru.urasha.callmeani.dream_marketplace.models.entities.ModerationLog;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.ModerationLogRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ModerationService {

    private final LotRepository lotRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final NotificationService notificationService;

    public ModerationService(LotRepository lotRepository,
                             ModerationLogRepository moderationLogRepository,
                             NotificationService notificationService) {
        this.lotRepository = lotRepository;
        this.moderationLogRepository = moderationLogRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ModerationQueueItemDto> listPendingLots() {
        return lotRepository.findByStatusOrderBySubmittedAtDesc(LotStatus.PENDING).stream()
                .map(this::toQueueDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ModerationLogDto> listLog() {
        return moderationLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toLogDto)
                .toList();
    }

    @Transactional
    public void approve(Long lotId, UserAccount admin) {
        Lot lot = lotRepository.findDetailedById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден"));
        if (lot.getStatus() != LotStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Лот не находится на модерации");
        }

        lot.setStatus(LotStatus.OPEN);
        lot.setReviewedAt(LocalDateTime.now());
        lot.setModerationReason(null);
        lotRepository.save(lot);

        ModerationLog log = new ModerationLog();
        log.setAdmin(admin);
        log.setLot(lot);
        log.setAction("approved");
        moderationLogRepository.save(log);

        notifyAuthor(lot, "Лот «" + lot.getTitle() + "» прошел модерацию и опубликован.");
    }

    @Transactional
    public void reject(Long lotId, UserAccount admin, String reason) {
        Lot lot = lotRepository.findDetailedById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден"));
        if (lot.getStatus() != LotStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Лот не находится на модерации");
        }

        lot.setStatus(LotStatus.CLOSED);
        lot.setReviewedAt(LocalDateTime.now());
        lot.setModerationReason(reason);
        lotRepository.save(lot);

        ModerationLog log = new ModerationLog();
        log.setAdmin(admin);
        log.setLot(lot);
        log.setAction("rejected");
        log.setReason(reason);
        moderationLogRepository.save(log);

        String message = "Лот «" + lot.getTitle() + "» отклонен модерацией";
        if (reason != null && !reason.isBlank()) {
            message += ". Причина: " + reason.trim();
        }
        notificationService.notifyUser(
            lot.getDreamRecord() != null ? lot.getDreamRecord().getUser() : null,
            message,
            lot.getId(),
            null
        );
    }

    private void notifyAuthor(Lot lot, String message) {
        if (lot == null) {
            return;
        }
        var dream = lot.getDreamRecord();
        var author = dream != null ? dream.getUser() : null;
        notificationService.notifyUser(author, message, lot.getId(), null);
    }

    private ModerationQueueItemDto toQueueDto(Lot lot) {
        var dream = lot.getDreamRecord();
        var author = dream != null ? dream.getUser() : null;
        List<String> tags = dream != null && dream.getTags() != null
                ? dream.getTags().stream().map(t -> t.getName()).toList()
                : Collections.emptyList();

        return new ModerationQueueItemDto(
                lot.getId(),
                lot.getTitle(),
            lot.getDescription(),
                author != null ? author.getUsername() : null,
                lot.getSubmittedAt(),
                lot.getPrice(),
                tags,
                "/api/lots/" + lot.getId() + "/preview"
        );
    }

    private ModerationLogDto toLogDto(ModerationLog log) {
        var lot = log.getLot();
        var admin = log.getAdmin();
        return new ModerationLogDto(
                log.getId(),
                lot != null ? lot.getId() : null,
                lot != null ? lot.getTitle() : null,
                admin != null ? admin.getUsername() : null,
                log.getAction(),
                log.getReason(),
                log.getCreatedAt()
        );
    }
}