package com.lexdata.paiements.controllers;

import com.lexdata.paiements.models.Subscription;
import com.lexdata.paiements.models.Transaction;
import com.lexdata.paiements.repository.SubscriptionRepository;
import com.lexdata.paiements.repository.TransactionRepository;
import com.lexdata.paiements.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
public class PaymentController {

    private final PaymentService paymentService;
    private final TransactionRepository transactionRepository;
    private final SubscriptionRepository subscriptionRepository;

    public PaymentController(PaymentService paymentService,
            TransactionRepository transactionRepository,
            SubscriptionRepository subscriptionRepository) {
        this.paymentService = paymentService;
        this.transactionRepository = transactionRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/subscription")
    public ResponseEntity<Subscription> getMySubscription() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return subscriptionRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Transaction> subscribe(@RequestParam("tier") String tier,
            @RequestParam("amount") double amount) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Transaction transaction = paymentService.initiatePayment(username, amount,
                Transaction.TransactionType.SUBSCRIPTION, tier);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions")
    public List<Transaction> getMyTransactions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return transactionRepository.findByUsername(username);
    }

    @PostMapping("/webhook-simulate")
    public ResponseEntity<String> simulateWebhook(@RequestParam("reference") String reference,
            @RequestParam("status") String status) {
        paymentService.processWebhook(reference, Transaction.TransactionStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok("Webhook processed");
    }
}
