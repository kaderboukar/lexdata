package com.lexdata.notifications.services;

import com.lexdata.notifications.models.ProcessedEvent;
import com.lexdata.notifications.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventDedupService {

    private final ProcessedEventRepository processedEventRepository;

    public boolean isAlreadyProcessed(String eventId) {
        return eventId != null && processedEventRepository.existsByEventId(eventId);
    }

    @Transactional
    public void markProcessed(String eventId, String eventType, String serviceSource) {
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(eventId)
                .eventType(eventType != null ? eventType : "UNKNOWN")
                .serviceSource(serviceSource)
                .build());
    }
}
