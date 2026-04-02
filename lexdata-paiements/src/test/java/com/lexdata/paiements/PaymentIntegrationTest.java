package com.lexdata.paiements;

import com.lexdata.paiements.models.Subscription;
import com.lexdata.paiements.models.Transaction;
import com.lexdata.paiements.repository.SubscriptionRepository;
import com.lexdata.paiements.repository.TransactionRepository;
import com.lexdata.paiements.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private SubscriptionRepository subscriptionRepository;

    @Test
    @WithMockUser(username = "premium_user")
    void getMySubscription_WhenExists_ShouldReturnSubscription() throws Exception {
        Subscription subscription = Subscription.builder()
                .username("premium_user")
                .tier(Subscription.SubscriptionTier.PREMIUM)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        when(subscriptionRepository.findByUsername("premium_user")).thenReturn(Optional.of(subscription));

        mockMvc.perform(get("/api/paiements/subscription"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tier").value("PREMIUM"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "new_user")
    void subscribe_ShouldInitiateTransaction() throws Exception {
        Transaction transaction = Transaction.builder()
                .paymentReference("TX-123")
                .amount(5000)
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        when(paymentService.initiatePayment("new_user", 5000.0, Transaction.TransactionType.SUBSCRIPTION, "PREMIUM"))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/paiements/subscribe")
                .param("tier", "PREMIUM")
                .param("amount", "5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentReference").value("TX-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
