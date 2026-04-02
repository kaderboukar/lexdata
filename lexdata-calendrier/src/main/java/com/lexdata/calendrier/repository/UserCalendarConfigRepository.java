package com.lexdata.calendrier.repository;

import com.lexdata.calendrier.models.UserCalendarConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCalendarConfigRepository extends JpaRepository<UserCalendarConfig, Long> {
    Optional<UserCalendarConfig> findByUsername(String username);
}
