package com.lexdata.veille.repository;

import com.lexdata.veille.models.VeilleSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VeilleSubscriptionRepository extends JpaRepository<VeilleSubscription, Long> {
    List<VeilleSubscription> findByUserIdOrderByCreatedAtDesc(String userId);
    List<VeilleSubscription> findByActiveTrue();
}
