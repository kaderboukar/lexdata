package com.lexdata.user.controllers;

import com.lexdata.user.dto.CompanyMembershipDto;
import com.lexdata.user.models.CompanyMembership;
import com.lexdata.user.models.UserProfile;
import com.lexdata.user.repository.CompanyMembershipRepository;
import com.lexdata.user.repository.UserProfileRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyMembershipRepository membershipRepository;
    private final UserProfileRepository profileRepository;

    @Data
    static class InviteRequest {
        private String email;
        private CompanyMembership.MembershipRole role;
    }

    // 1. Inviter un employé
    @PostMapping("/invite")
    public ResponseEntity<CompanyMembershipDto> inviteEmployee(@RequestBody InviteRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile companyProfile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Profil entreprise non trouve"));

        CompanyMembership membership = CompanyMembership.builder()
                .companyProfile(companyProfile)
                .employeeEmail(request.getEmail())
                .role(request.getRole())
                .status(CompanyMembership.MembershipStatus.PENDING)
                .build();

        return ResponseEntity.ok(toDto(membershipRepository.save(membership)));
    }

    // 2. Lister les membres de mon entreprise
    @GetMapping("/members")
    public ResponseEntity<List<CompanyMembershipDto>> getMyMembers() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserProfile companyProfile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Profil entreprise non trouve"));

        return ResponseEntity.ok(membershipRepository.findByCompanyProfile(companyProfile).stream().map(this::toDto).toList());
    }

    private CompanyMembershipDto toDto(CompanyMembership membership) {
        return CompanyMembershipDto.builder()
                .id(membership.getId())
                .employeeEmail(membership.getEmployeeEmail())
                .role(membership.getRole())
                .status(membership.getStatus())
                .build();
    }
}
