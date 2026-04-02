package com.lexdata.notifications.repository;

import com.lexdata.notifications.models.NotificationDelivery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

    @Query("""
            SELECT d FROM NotificationDelivery d
            WHERE d.status = :failed
            AND d.retryCount < :maxRetries
            AND d.nextRetryAt IS NOT NULL
            AND d.nextRetryAt <= :now
            AND d.digestHold = false
            ORDER BY d.nextRetryAt ASC
            """)
    List<NotificationDelivery> findDueForRetry(
            @Param("failed") NotificationDelivery.DeliveryStatus failed,
            @Param("maxRetries") int maxRetries,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
