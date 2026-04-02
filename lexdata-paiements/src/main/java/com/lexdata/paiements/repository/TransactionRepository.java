package com.lexdata.paiements.repository;

import com.lexdata.paiements.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsername(String username);
    Optional<Transaction> findByPaymentReference(String reference);
}
