package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.dto.PurchaseItemDto;
import ru.urasha.callmeani.dream_marketplace.dto.TransactionDto;
import ru.urasha.callmeani.dream_marketplace.models.entities.Lot;
import ru.urasha.callmeani.dream_marketplace.models.entities.Transaction;
import ru.urasha.callmeani.dream_marketplace.models.entities.UserAccount;
import ru.urasha.callmeani.dream_marketplace.models.enums.LotStatus;
import ru.urasha.callmeani.dream_marketplace.repositories.LotRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.TransactionRepository;
import ru.urasha.callmeani.dream_marketplace.repositories.UserAccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseService {

    private final LotRepository lotRepository;
    private final UserAccountRepository userAccountRepository;
    private final TransactionRepository transactionRepository;

    public PurchaseService(LotRepository lotRepository,
                           UserAccountRepository userAccountRepository,
                           TransactionRepository transactionRepository) {
        this.lotRepository = lotRepository;
        this.userAccountRepository = userAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionDto buyLot(Long lotId, Long buyerId) {
        Lot lot = lotRepository.findDetailedById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Лот не найден"));

        if (lot.getStatus() != LotStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Лот недоступен для покупки");
        }

        UserAccount buyer = userAccountRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Покупатель не найден"));

        UserAccount seller = lot.getDreamRecord().getUser();
        if (seller == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "У лота нет владельца");
        }
        if (seller.getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя купить свой лот");
        }

        BigDecimal price = lot.getPrice();
        if (buyer.getBalance().compareTo(price) < 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Недостаточно средств на балансе");
        }

        buyer.setBalance(buyer.getBalance().subtract(price));
        seller.setBalance(seller.getBalance().add(price));

        lot.setStatus(LotStatus.SOLD);

        Transaction tx = new Transaction();
        tx.setLot(lot);
        tx.setBuyer(buyer);
        tx.setSeller(seller);
        tx.setAmount(price);
        tx.setFee(BigDecimal.ZERO);

        transactionRepository.save(tx);
        userAccountRepository.save(buyer);
        userAccountRepository.save(seller);
        lotRepository.save(lot);

        return new TransactionDto(tx.getId(), lot.getId(), buyer.getId(), seller.getId(), price, tx.getFee(), tx.getTransactionDate());
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemDto> listPurchases(Long buyerId) {
        return transactionRepository.findByBuyer_IdOrderByTransactionDateDesc(buyerId).stream()
                .map(tx -> {
                    var lot = tx.getLot();
                    var dream = lot != null ? lot.getDreamRecord() : null;
                    var author = dream != null ? dream.getUser() : null;
                    var visualization = dream != null ? dream.getVisualization() : null;
                    return new PurchaseItemDto(
                            tx.getId(),
                            lot != null ? lot.getId() : null,
                            lot != null ? lot.getTitle() : null,
                            lot != null ? lot.getDescription() : null,
                            tx.getAmount(),
                            tx.getTransactionDate(),
                            visualization != null && lot != null ? "/api/lots/" + lot.getId() + "/preview" : null,
                            author != null ? author.getId() : null,
                            author != null ? author.getUsername() : null
                    );
                })
                .toList();
    }
}
