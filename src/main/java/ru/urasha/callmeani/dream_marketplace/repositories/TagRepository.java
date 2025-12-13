package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.Tag;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByIdIn(Iterable<Long> ids);
}
