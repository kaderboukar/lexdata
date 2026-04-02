package com.lexdata.user.repository;

import com.lexdata.user.models.UserPreference;
import com.lexdata.user.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserProfile(UserProfile userProfile);

    Optional<UserPreference> findByUserProfileUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT p.userProfile.username FROM UserPreference p JOIN p.followedTopics t WHERE t = :domain")
    java.util.List<String> findUsernamesByFollowedTopic(com.lexdata.user.models.LegalDomain domain);
}
