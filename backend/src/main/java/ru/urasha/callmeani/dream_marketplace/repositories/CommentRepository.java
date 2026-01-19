package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLot_IdOrderByCreatedAtDesc(Long lotId);

    void deleteByLot_Id(Long lotId);
}
