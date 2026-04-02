package com.lexdata.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private String code;
    private String message;
    private String timestamp;
    private String traceId;
}

