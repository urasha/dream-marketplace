package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.Category;
import ru.urasha.callmeani.dream_marketplace.models.entities.DreamRecord;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;
import ru.urasha.callmeani.dream_marketplace.models.entities.Tag;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;
import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.CategoryRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.DreamRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.TagRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.TransactionRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.VisualizationRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LotService {

    private final LotRepository lotRepository;
    private final VisualizationRepository visualizationRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final DreamRepository dreamRepository;
    private final TransactionRepository transactionRepository;

    public LotService(LotRepository lotRepository,
                      VisualizationRepository visualizationRepository,
                      CategoryRepository categoryRepository,
                      TagRepository tagRepository,
                      DreamRepository dreamRepository,
                      TransactionRepository transactionRepository) {
        this.lotRepository = lotRepository;
        this.visualizationRepository = visualizationRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.dreamRepository = dreamRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Lot createLotFromVisualization(Long visualizationId,
                                          UserAccount owner,
                                          String title,
                                          String description,
                                          BigDecimal price,
                                          Long categoryId,
                                          List<Long> tagIds,
                                          List<String> tagNames) {
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
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Для этой визуализации уже опубликован лот. Нельзя создать второй." );
        }

        Lot lot = new Lot();
        lot.setDreamRecord(dream);
        lot.setTitle(title);
        lot.setDescription(description);
        lot.setPrice(price);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            dream.setCategory(category);
        }

        Set<Tag> resultingTags = new HashSet<>();

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findByIdIn(tagIds);
            resultingTags.addAll(tags);
        }

        if (tagNames != null && !tagNames.isEmpty()) {
            for (String rawName : tagNames) {
                if (rawName == null) {
                    continue;
                }
                String name = rawName.trim();
                if (name.isEmpty()) {
                    continue;
                }
                Tag tag = tagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> {
                            Tag t = new Tag();
                            t.setName(name);
                            return tagRepository.save(t);
                        });
                resultingTags.add(tag);
            }
        }

        if (!resultingTags.isEmpty()) {
            dream.setTags(resultingTags);
        }

        dreamRepository.save(dream);

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
    public Lot getLotForPreview(Long id) {
        return lotRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
    }

    @Transactional(readOnly = true)
        public Lot getLotForPublic(Long id, UserAccount currentUser) {
        Lot lot = lotRepository.findDetailedById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        boolean isOwner = currentUser != null
            && lot.getDreamRecord() != null
            && lot.getDreamRecord().getUser() != null
            && lot.getDreamRecord().getUser().getId().equals(currentUser.getId());

        boolean isBuyer = currentUser != null
            && transactionRepository.existsByLot_IdAndBuyer_Id(lot.getId(), currentUser.getId());

        if (lot.getStatus() != LotStatus.OPEN && !isOwner && !isBuyer) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not available");
        }
        return lot;
    }
}
