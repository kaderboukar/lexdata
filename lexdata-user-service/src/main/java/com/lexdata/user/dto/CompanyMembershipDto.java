package com.lexdata.user.dto;

import com.lexdata.user.models.CompanyMembership;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyMembershipDto {
    private Long id;
    private String employeeEmail;
    private CompanyMembership.MembershipRole role;
    private CompanyMembership.MembershipStatus status;
}
