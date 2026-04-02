package com.lexdata.auth.dto.internal;

import java.util.List;

public record UserIdPageResponse(List<Long> content, long totalElements, int totalPages, int number) {
}
