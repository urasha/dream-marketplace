package ru.urasha.callmeani.dream_marketplace.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urasha.callmeani.dream_marketplace.models.entities.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
	@EntityGraph(attributePaths = {
			"lot",
			"lot.dreamRecord",
			"lot.dreamRecord.user",
			"lot.dreamRecord.category",
			"lot.dreamRecord.tags",
			"lot.dreamRecord.visualization"
	})
	List<Transaction> findByBuyer_IdOrderByTransactionDateDesc(Long buyerId);

	boolean existsByLot_IdAndBuyer_Id(Long lotId, Long buyerId);
}