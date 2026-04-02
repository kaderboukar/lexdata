package com.lexdata.monitoring.controllers;

import com.lexdata.monitoring.models.AlertRule;
import com.lexdata.monitoring.models.TechnicalLog;
import com.lexdata.monitoring.repository.AlertRuleRepository;
import com.lexdata.monitoring.repository.TechnicalLogRepository;
import com.lexdata.monitoring.services.MonitoringService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitoringControllerTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private TechnicalLogRepository logRepository;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @InjectMocks
    private MonitoringController monitoringController;

    @Test
    void checkGlobalHealth_ShouldReturnUpStatus() {
        // Act
        ResponseEntity<Map<String, String>> response = monitoringController.checkGlobalHealth();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("system"));
    }

    @Test
    void reportError_ShouldCallService() {
        // Arrange
        TechnicalLog log = new TechnicalLog();
        log.setServiceName("test-service");
        log.setMessage("Test Error");

        // Act
        ResponseEntity<String> response = monitoringController.reportError(log);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        verify(monitoringService).logError(any(TechnicalLog.class));
    }

    @Test
    void getAlertRules_ShouldReturnList() {
        // Arrange
        when(alertRuleRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<AlertRule> result = monitoringController.getAlertRules();

        // Assert
        assertNotNull(result);
        verify(alertRuleRepository).findAll();
    }
}
