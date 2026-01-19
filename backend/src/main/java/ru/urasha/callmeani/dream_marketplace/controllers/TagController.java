package ru.urasha.callmeani.dream_marketplace.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.urasha.callmeani.dream_marketplace.dto.TagDto;
import ru.urasha.callmeani.dream_marketplace.repositories.TagRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> search(@RequestParam(name = "q", required = false) String query,
                                               @RequestParam(name = "limit", defaultValue = "20") int limit) {
        String q = query != null ? query.trim() : "";
        int pageSize = Math.max(1, Math.min(100, limit));
        var pageable = PageRequest.of(0, pageSize);
        var tags = q.isEmpty()
            ? tagRepository.findAllByOrderByNameAsc(pageable)
            : tagRepository.findByNameContainingIgnoreCaseOrderByNameAsc(q, pageable);
        var dtos = tags.stream().map(t -> new TagDto(t.getId(), t.getName())).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<TagDto>> byIds(@RequestParam(name = "ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        var tags = tagRepository.findByIdIn(ids);
        var dtos = tags.stream().map(t -> new TagDto(t.getId(), t.getName())).toList();
        return ResponseEntity.ok(dtos);
    }
}
