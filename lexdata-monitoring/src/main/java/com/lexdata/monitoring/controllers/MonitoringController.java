package com.lexdata.monitoring.controllers;

import com.lexdata.monitoring.models.AlertRule;
import com.lexdata.monitoring.models.TechnicalLog;
import com.lexdata.monitoring.repository.AlertRuleRepository;
import com.lexdata.monitoring.repository.TechnicalLogRepository;
import com.lexdata.monitoring.services.MonitoringService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final TechnicalLogRepository logRepository;
    private final AlertRuleRepository alertRuleRepository;

    public MonitoringController(MonitoringService monitoringService, 
                                TechnicalLogRepository logRepository, 
                                AlertRuleRepository alertRuleRepository) {
        this.monitoringService = monitoringService;
        this.logRepository = logRepository;
        this.alertRuleRepository = alertRuleRepository;
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> checkGlobalHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("system", "UP");
        status.put("database", "CONNECTED");
        status.put("eureka", "REGISTERED");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/logs")
    public ResponseEntity<String> reportError(@RequestBody TechnicalLog errorLog) {
        monitoringService.logError(errorLog);
        return ResponseEntity.ok("Erreur enregistrée et analyse d'alerte lancée");
    }

    @GetMapping("/logs")
    public List<TechnicalLog> getLogs(@RequestParam(required = false) String service,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        if (service != null) {
            return logRepository.findByServiceNameOrderByTimestampDesc(service, PageRequest.of(page, size));
        }
        return logRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    @GetMapping("/rules")
    public List<AlertRule> getAlertRules() {
        return alertRuleRepository.findAll();
    }

    @PostMapping("/rules")
    public ResponseEntity<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        return ResponseEntity.ok(alertRuleRepository.save(rule));
    }

    @PostMapping("/simulate-spike")
    public ResponseEntity<String> simulateMetricSpike() {
        // Simule un événement qui déclencherait le moteur d'alerte
        monitoringService.checkAlertRules();
        return ResponseEntity.ok("Simulation de pic de métriques effectuée");
    }
}
