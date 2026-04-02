package com.lexdata.paiements.services;

import com.lexdata.paiements.models.Subscription;
import com.lexdata.paiements.models.Transaction;
import com.lexdata.paiements.repository.SubscriptionRepository;
import com.lexdata.paiements.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(SubscriptionRepository subscriptionRepository, 
                          TransactionRepository transactionRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction initiatePayment(String username, double amount, Transaction.TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setUsername(username);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setPaymentReference("LX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        return transactionRepository.save(transaction);
    }

    public void processWebhook(String reference, Transaction.TransactionStatus status) {
        transactionRepository.findByPaymentReference(reference).ifPresent(transaction -> {
            transaction.setStatus(status);
            transactionRepository.save(transaction);

            if (status == Transaction.TransactionStatus.SUCCESS && transaction.getType() == Transaction.TransactionType.SUBSCRIPTION) {
                activateSubscription(transaction.getUsername(), transaction.getDescription());
            }
        });
    }

    private void activateSubscription(String username, String tierName) {
        Subscription.SubscriptionTier tier = Subscription.SubscriptionTier.valueOf(tierName.toUpperCase());
        Subscription sub = subscriptionRepository.findByUsername(username)
                .orElse(new Subscription());
        
        sub.setUsername(username);
        sub.setTier(tier);
        sub.setStartDate(LocalDateTime.now());
        sub.setEndDate(LocalDateTime.now().plusMonths(1));
        sub.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);
    }
}
