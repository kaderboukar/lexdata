package com.lexdata.calendrier.services;

import com.lexdata.calendrier.models.CalendarEvent;
import com.lexdata.calendrier.models.LegalObligationRule;
import com.lexdata.calendrier.models.UserCalendarConfig;
import com.lexdata.calendrier.repository.LegalObligationRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendrierService {

    public LocalDateTime calculerEcheance(LegalObligationRule rule, UserCalendarConfig config, LocalDate dateReference) {
        String regle = rule.getRegleCalcul();
        
        if ("D+15_MOIS_SUIVANT".equals(regle)) {
            // ex: TVA
            LocalDate nextMonth = dateReference.plusMonths(1);
            return LocalDateTime.of(nextMonth.withDayOfMonth(rule.getDelaiJours()), LocalTime.of(23, 59));
        } else if ("CLOTURE+4_MOIS".equals(regle)) {
            // ex: IS
            if (config.getDateClotureExercice() == null) return null;
            
            // On prend l'année de référence
            LocalDate cloture = config.getDateClotureExercice().withYear(dateReference.getYear());
            if (cloture.isBefore(dateReference)) {
                cloture = cloture.plusYears(1);
            }
            return LocalDateTime.of(cloture.plusMonths(4).withDayOfMonth(30), LocalTime.of(23, 59));
        } else if ("CLOTURE+6_MOIS".equals(regle)) {
            // ex: Assemblée Générale
            if (config.getDateClotureExercice() == null) return null;
            LocalDate cloture = config.getDateClotureExercice().withYear(dateReference.getYear());
            return LocalDateTime.of(cloture.plusMonths(6).withDayOfMonth(30), LocalTime.of(23, 59));
        }
        
        return null;
    }

    public List<CalendarEvent> genererEvenements(UserCalendarConfig config, List<LegalObligationRule> rules) {
        List<CalendarEvent> events = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (LegalObligationRule rule : rules) {
            LocalDateTime echeance = calculerEcheance(rule, config, now);
            if (echeance != null) {
                CalendarEvent event = new CalendarEvent();
                event.setUsername(config.getUsername());
                event.setRule(rule);
                event.setTitre(rule.getTitre());
                event.setDescription(rule.getDescription());
                event.setDateEcheance(echeance);
                event.setStatus(CalendarEvent.EventStatus.A_VENIR);
                event.setManuel(false);
                events.add(event);
            }
        }
        return events;
    }
}
