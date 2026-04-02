package com.lexdata.veille.repository;

import com.lexdata.veille.models.UserAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAlertRepository extends JpaRepository<UserAlert, Long> {
    boolean existsByUserIdAndAlertId(String userId, Long alertId);
    Optional<UserAlert> findByIdAndUserId(Long id, String userId);

    @Query("""
            SELECT ua FROM UserAlert ua
            WHERE ua.userId = :userId
              AND ua.deletedAt IS NULL
              AND (:readFilter IS NULL OR ua.isRead = :readFilter)
              AND (:domaine IS NULL OR EXISTS (
                    SELECT d FROM ua.alert.domainesCibles d WHERE d = :domaine
              ))
              AND (:textType IS NULL OR ua.alert.texteType = :textType)
              AND ua.alert.dateCreation >= COALESCE(:fromDate, ua.alert.dateCreation)
              AND ua.alert.dateCreation <= COALESCE(:toDate, ua.alert.dateCreation)
            ORDER BY ua.alert.dateCreation DESC
            """)
    Page<UserAlert> searchMyAlerts(
            @Param("userId") String userId,
            @Param("readFilter") Boolean readFilter,
            @Param("domaine") String domaine,
            @Param("textType") String textType,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            Pageable pageable
    );
}
