package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.CommentCreateRequest;
import ru.urasha.callmeani.dream_marketplace.dto.CommentDto;
import ru.urasha.callmeani.dream_marketplace.dto.RatingSummaryDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.Comment;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;
import ru.urasha.callmeani.dream_marketplace.models.entities.Rating;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.CommentRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.RatingRepository;

import java.util.List;

@Service
public class LotFeedbackService {

    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final LotRepository lotRepository;
    private final NotificationService notificationService;

    public LotFeedbackService(CommentRepository commentRepository,
                              RatingRepository ratingRepository,
                              LotRepository lotRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.ratingRepository = ratingRepository;
        this.lotRepository = lotRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listComments(Long lotId) {
        ensureLotExists(lotId);
        return commentRepository.findByLot_IdOrderByCreatedAtDesc(lotId)
                .stream()
                .map(this::toCommentDto)
                .toList();
    }

    @Transactional
    public CommentDto addComment(Long lotId, UserAccount user, CommentCreateRequest request) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден"));
        if (lot.getStatus() == LotStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Лот закрыт: комментарии недоступны");
        }

        Comment comment = new Comment();
        comment.setLot(lot);
        comment.setUser(user);
        comment.setContent(request.content().trim());
        Comment saved = commentRepository.save(comment);

        notifyAuthorIfNeeded(
            lot,
            user,
            "Пользователь " + displayName(user) + " оставил комментарий к лоту «" + lot.getTitle() + "».",
            lot.getId(),
            saved.getId()
        );
        return toCommentDto(saved);
    }

    @Transactional
    public RatingSummaryDto setRating(Long lotId, UserAccount user, int value) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден"));
        if (lot.getStatus() == LotStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Лот закрыт: рейтинг недоступен");
        }

        Rating rating = ratingRepository.findByLot_IdAndUser_Id(lotId, user.getId())
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setLot(lot);
                    r.setUser(user);
                    return r;
                });
        boolean isNewRating = rating.getId() == null;
        rating.setValue(value);
        ratingRepository.save(rating);

        if (isNewRating) {
                notifyAuthorIfNeeded(
                    lot,
                    user,
                    "Пользователь " + displayName(user) + " поставил оценку " + value + " лоту «" + lot.getTitle() + "».",
                    lot.getId(),
                    null
                );
        }

        double avg = ratingRepository.averageForLot(lotId);
        long count = ratingRepository.countForLot(lotId);
        return new RatingSummaryDto(avg, count, value);
    }

    @Transactional(readOnly = true)
    public RatingSummaryDto getRating(Long lotId, UserAccount currentUser) {
        ensureLotExists(lotId);
        Integer userValue = null;
        if (currentUser != null) {
            userValue = ratingRepository.findByLot_IdAndUser_Id(lotId, currentUser.getId())
                    .map(Rating::getValue)
                    .orElse(null);
        }
        double avg = ratingRepository.averageForLot(lotId);
        long count = ratingRepository.countForLot(lotId);
        return new RatingSummaryDto(avg, count, userValue);
    }

    private void ensureLotExists(Long lotId) {
        if (!lotRepository.existsById(lotId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден");
        }
    }

    private CommentDto toCommentDto(Comment comment) {
        var user = comment.getUser();
        return new CommentDto(
                comment.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    private void notifyAuthorIfNeeded(Lot lot, UserAccount actor, String message, Long targetLotId, Long targetCommentId) {
        if (lot == null) {
            return;
        }
        var dream = lot.getDreamRecord();
        var author = dream != null ? dream.getUser() : null;
        if (author == null || actor == null || author.getId() == null || actor.getId() == null) {
            return;
        }
        if (author.getId().equals(actor.getId())) {
            return;
        }
        notificationService.notifyUser(author, message, targetLotId, targetCommentId);
    }

    private String displayName(UserAccount user) {
        if (user == null) {
            return "неизвестный пользователь";
        }
        return user.getUsername() != null ? user.getUsername() : "неизвестный пользователь";
    }
}
