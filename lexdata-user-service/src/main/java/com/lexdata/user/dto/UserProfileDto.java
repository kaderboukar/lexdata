package com.lexdata.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String username;
    private String fullName;
    private String phoneNumber;
    private String city;
    private String profilePictureUrl;
    private String preferredLanguage;
    private String bio;
    private java.util.Set<com.lexdata.user.models.LegalDomain> specialties;
    private String professionalTitle;
    private String availability;
    private String barreau;
    private String numeroToque;
    private String verificationStatus;
    private String companyName;
    private String nif;
    private Integer employeeCount;
    private String subscriptionType;
}
