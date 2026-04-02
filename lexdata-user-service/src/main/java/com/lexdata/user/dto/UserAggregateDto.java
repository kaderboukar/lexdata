package com.lexdata.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAggregateDto {
    private UserProfileDto profile;
    private UserPreferenceDto preference;
}
