package com.lexdata.paiements.controllers;

import com.lexdata.paiements.models.Subscription;
import com.lexdata.paiements.models.Transaction;
import com.lexdata.paiements.repository.SubscriptionRepository;
import com.lexdata.paiements.repository.TransactionRepository;
import com.lexdata.paiements.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test_user");
    }

    @Test
    void getMySubscription_ShouldReturnSubscription() {
        // Arrange
        Subscription sub = new Subscription();
        sub.setUsername("test_user");
        when(subscriptionRepository.findByUsername("test_user")).thenReturn(Optional.of(sub));

        // Act
        ResponseEntity<Subscription> response = paymentController.getMySubscription();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(subscriptionRepository).findByUsername("test_user");
    }

    @Test
    void subscribe_ShouldCallServiceAndReturnTransaction() {
        // Arrange
        Transaction tx = new Transaction();
        tx.setUsername("test_user");
        when(paymentService.initiatePayment(eq("test_user"), anyDouble(), any(), anyString())).thenReturn(tx);

        // Act
        ResponseEntity<Transaction> response = paymentController.subscribe("PREMIUM", 5000.0);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(paymentService).initiatePayment(eq("test_user"), eq(5000.0), any(), eq("PREMIUM"));
    }

    @Test
    void getMyTransactions_ShouldReturnList() {
        // Arrange
        when(transactionRepository.findByUsername("test_user")).thenReturn(Collections.emptyList());

        // Act
        List<Transaction> result = paymentController.getMyTransactions();

        // Assert
        assertNotNull(result);
        verify(transactionRepository).findByUsername("test_user");
    }
}
