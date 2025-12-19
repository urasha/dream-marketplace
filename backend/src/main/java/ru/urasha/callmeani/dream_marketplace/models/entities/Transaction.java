package ru.urasha.callmeani.dream_marketplace.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private UserAccount buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private UserAccount seller;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private Lot lot;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }
}