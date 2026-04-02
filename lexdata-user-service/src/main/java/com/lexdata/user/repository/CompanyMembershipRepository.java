package com.lexdata.user.repository;

import com.lexdata.user.models.CompanyMembership;
import com.lexdata.user.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
    List<CompanyMembership> findByCompanyProfile(UserProfile companyProfile);

    List<CompanyMembership> findByEmployeeEmail(String email);
}
