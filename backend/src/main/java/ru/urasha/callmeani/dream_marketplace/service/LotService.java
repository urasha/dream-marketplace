package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.DreamRecord;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;
import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.VisualizationRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LotService {

    private final LotRepository lotRepository;
    private final VisualizationRepository visualizationRepository;

    public LotService(LotRepository lotRepository, VisualizationRepository visualizationRepository) {
        this.lotRepository = lotRepository;
        this.visualizationRepository = visualizationRepository;
    }

    @Transactional
    public Lot createLotFromVisualization(Long visualizationId, UserAccount owner, String title, String description, BigDecimal price) {
        Visualization visualization = visualizationRepository.findById(visualizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visualization not found"));

        DreamRecord dream = visualization.getDreamRecord();
        if (dream == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visualization is not linked to a dream");
        }
        if (!dream.getUser().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can create lots only for your own visualizations");
        }
        if (visualization.getStatus() != VisualizationStatus.ACCEPTED && visualization.getStatus() != VisualizationStatus.READY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visualization is not ready for publishing");
        }
        if (lotRepository.existsByDreamRecordId(dream.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lot already exists for this dream");
        }

        Lot lot = new Lot();
        lot.setDreamRecord(dream);
        lot.setTitle(title);
        lot.setDescription(description);
        lot.setPrice(price);
        return lotRepository.save(lot);
    }

    @Transactional(readOnly = true)
    public List<Lot> listOpen() {
        return lotRepository.findByStatusOrderBySubmittedAtDesc(LotStatus.OPEN);
    }

    @Transactional(readOnly = true)
    public List<Lot> listOwn(UserAccount owner) {
        return lotRepository.findByDreamRecord_User_IdOrderBySubmittedAtDesc(owner.getId());
    }

    @Transactional(readOnly = true)
        public Lot getLotForPublic(Long id, UserAccount currentUser) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        boolean isOwner = currentUser != null
            && lot.getDreamRecord() != null
            && lot.getDreamRecord().getUser() != null
            && lot.getDreamRecord().getUser().getId().equals(currentUser.getId());

        if (lot.getStatus() != LotStatus.OPEN && !isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not available");
        }
        return lot;
    }
}
