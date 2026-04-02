package com.lexdata.user.dto;

import com.lexdata.user.models.LegalDomain;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserPreferenceDto {
    private Set<LegalDomain> followedTopics;
    private Set<String> alertKeywords;
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean smsEnabled;
    private String timezone;
}
