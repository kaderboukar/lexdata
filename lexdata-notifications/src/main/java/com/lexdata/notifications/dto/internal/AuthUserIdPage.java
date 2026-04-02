package com.lexdata.notifications.dto.internal;

import java.util.List;

public record AuthUserIdPage(List<Long> content, long totalElements, int totalPages, int number) {
}
