package com.lexdata.paiements.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private double amount;
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    private String paymentReference; // Référence Paystack ou Wave
    private String gateway = "PAYSTACK";

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    public enum TransactionType {
        SUBSCRIPTION, UNITARY_CONSULTATION, UNITARY_FORMATION, AUDIT, LEAD_COMMISSION
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }
}
