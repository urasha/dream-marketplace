package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.urasha.callmeani.dream_marketplace.config.MarketplaceProperties;
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
import java.math.RoundingMode;
import java.util.List;

@Service
public class PurchaseService {

    private final LotRepository lotRepository;
    private final UserAccountRepository userAccountRepository;
    private final TransactionRepository transactionRepository;
    private final MarketplaceProperties marketplaceProperties;
    private final PlatformWalletService platformWalletService;

    public PurchaseService(LotRepository lotRepository,
                           UserAccountRepository userAccountRepository,
                           TransactionRepository transactionRepository,
                           MarketplaceProperties marketplaceProperties,
                           PlatformWalletService platformWalletService) {
        this.lotRepository = lotRepository;
        this.userAccountRepository = userAccountRepository;
        this.transactionRepository = transactionRepository;
        this.marketplaceProperties = marketplaceProperties;
        this.platformWalletService = platformWalletService;
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

        BigDecimal feePercent = marketplaceProperties.getFeePercent() != null
            ? marketplaceProperties.getFeePercent()
            : BigDecimal.ZERO;
        if (feePercent.compareTo(BigDecimal.ZERO) < 0) {
            feePercent = BigDecimal.ZERO;
        }
        BigDecimal fee = price
            .multiply(feePercent)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (fee.compareTo(price) > 0) {
            fee = price;
        }
        BigDecimal sellerNet = price.subtract(fee);

        buyer.setBalance(buyer.getBalance().subtract(price));
        seller.setBalance(seller.getBalance().add(sellerNet));
        platformWalletService.addCommission(fee);

        lot.setStatus(LotStatus.SOLD);

        Transaction tx = new Transaction();
        tx.setLot(lot);
        tx.setBuyer(buyer);
        tx.setSeller(seller);
        tx.setAmount(price);
        tx.setFee(fee);

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
