package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.models.entities.Category;
import ru.urasha.callmeani.dream_marketplace.models.entities.DreamRecord;
import ru.urasha.callmeani.dream_marketplace.models.entities.Tag;
import ru.urasha.callmeani.dream_marketplace.models.entities.Visualization;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.Privacy;
import ru.urasha.callmeani.dream_marketplace.models.enums.VisualizationStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.CategoryRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.DreamRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.TagRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.VisualizationRepository;
import ru.urasha.callmeani.dream_marketplace.service.dto.VisualizationRequestMessage;
import ru.urasha.callmeani.dream_marketplace.dto.VisualizationUploadRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DreamService {

    private final DreamRepository dreamRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final VisualizationRepository visualizationRepository;
    private final VisualizationProducer visualizationProducer;

    public DreamService(DreamRepository dreamRepository,
                        CategoryRepository categoryRepository,
                        TagRepository tagRepository,
                        VisualizationRepository visualizationRepository,
                        VisualizationProducer visualizationProducer) {
        this.dreamRepository = dreamRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.visualizationRepository = visualizationRepository;
        this.visualizationProducer = visualizationProducer;
    }

    @Transactional
    public DreamRecord create(UserAccount user, String title, String content, Privacy privacy, Long categoryId, List<Long> tagIds) {
        DreamRecord dream = new DreamRecord();
        dream.setUser(user);
        dream.setTitle(title);
        dream.setContent(content);
        dream.setPrivacy(privacy);
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            dream.setCategory(category);
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagRepository.findByIdIn(tagIds);
            dream.setTags(new HashSet<>(tags));
        }
        return dreamRepository.save(dream);
    }

    public List<DreamRecord> findOwn(UserAccount user) {
        return dreamRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Visualization requestVisualization(Long dreamId, UserAccount user) {
        DreamRecord dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dream not found"));
        if (!dream.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (dream.getVisualization() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Visualization already accepted for this dream");
        }
        Visualization vis = new Visualization();
        vis.setDreamRecord(dream);
        vis.setPrompt(dream.getContent());
        vis.setGenerator("GenAPI");
        vis.setStatus(VisualizationStatus.PENDING);
        visualizationRepository.save(vis);

        visualizationProducer.send(new VisualizationRequestMessage(dream.getId(), vis.getId(), vis.getPrompt()));
        return vis;
    }

    public List<Visualization> getVisualizations(Long dreamId, UserAccount user) {
        DreamRecord dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dream not found"));
        if (!dream.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return visualizationRepository.findByDreamRecordIdOrderByCreatedAtDesc(dreamId);
    }

    @Transactional
    public Visualization acceptVisualization(Long visualizationId, UserAccount user) {
        Visualization vis = visualizationRepository.findById(visualizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visualization not found"));
        DreamRecord dream = vis.getDreamRecord();
        if (dream == null || !dream.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        vis.setStatus(VisualizationStatus.ACCEPTED);
        dream.setVisualization(vis);
        return vis;
    }

    @Transactional
    public Visualization attachReadyVisualization(Long dreamId, UserAccount user, VisualizationUploadRequest request) {
        DreamRecord dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dream not found"));
        if (!dream.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (request == null || request.filePath() == null || request.filePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filePath is required");
        }

        Visualization vis = new Visualization();
        vis.setDreamRecord(dream);
        vis.setPrompt(dream.getContent());
        vis.setGenerator(request.generator() == null || request.generator().isBlank() ? "ImagesAPI" : request.generator());
        vis.setFilePath(request.filePath());
        vis.setMime(request.mime());
        vis.setWidth(request.width());
        vis.setHeight(request.height());
        vis.setDuration(request.duration());
        vis.setStatus(VisualizationStatus.ACCEPTED);
        visualizationRepository.save(vis);

        dream.setVisualization(vis);
        return vis;
    }
}
