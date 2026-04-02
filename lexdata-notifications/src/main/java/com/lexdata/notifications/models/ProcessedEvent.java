package com.lexdata.notifications.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String eventId;

    @Column(nullable = false, length = 128)
    private String eventType;

    @Column(nullable = false)
    private String serviceSource;

    @CreationTimestamp
    private LocalDateTime processedAt;
}
