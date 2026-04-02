package com.lexdata.veille.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Harmonise les codes domaine (formulaire admin court, enums juridique, legalDomains user-service)
 * vers un identifiant canonique unique pour le matching des abonnements veille.
 */
public final class VeilleDomainNormalizer {

    private static final Map<String, String> TO_CANONICAL = new HashMap<>();

    static {
        TO_CANONICAL.put("AFFAIRES", "DROIT_AFFAIRES");
        TO_CANONICAL.put("PENAL", "DROIT_PENAL");
        TO_CANONICAL.put("TRAVAIL", "DROIT_TRAVAIL");
        TO_CANONICAL.put("FISCAL", "DROIT_FISCAL");
        TO_CANONICAL.put("ADMINISTRATIF", "DROIT_ADMINISTRATIF");
        TO_CANONICAL.put("CONSTITUTIONNEL", "DROIT_CONSTITUTIONNEL");
        TO_CANONICAL.put("UEMOA", "DROIT_COMMUNAUTAIRE_UEMOA");
        TO_CANONICAL.put("CEDEAO", "DROIT_COMMUNAUTAIRE_CEDEAO");

        TO_CANONICAL.put("DROIT_DES_AFFAIRES", "DROIT_AFFAIRES");
        TO_CANONICAL.put("DROIT_SOCIAL_ET_TRAVAIL", "DROIT_TRAVAIL");
        TO_CANONICAL.put("FISCALITE_ET_DOUANES", "DROIT_FISCAL");
        TO_CANONICAL.put("DROIT_PUBLIC_ET_ADMINISTRATIF", "DROIT_ADMINISTRATIF");

        TO_CANONICAL.put("DROIT_CIVIL", "AUTRE");
        TO_CANONICAL.put("DROIT_IMMOBILIER_ET_FONCIER", "AUTRE");
        TO_CANONICAL.put("PROPRIETE_INTELLECTUELLE", "AUTRE");
        TO_CANONICAL.put("DROIT_ENVIRONNEMENTAL", "AUTRE");
        TO_CANONICAL.put("DROIT_INTERNATIONAL", "AUTRE");
        TO_CANONICAL.put("DROIT_NUMERIQUE_ET_DONNEES", "AUTRE");
    }

    private VeilleDomainNormalizer() {
    }

    /**
     * @return code canonique (ex. DROIT_FISCAL), ou null si entrée vide
     */
    public static String canonical(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String u = raw.trim().toUpperCase(Locale.ROOT);
        return TO_CANONICAL.getOrDefault(u, u);
    }

    public static Set<String> canonicalDomainSet(Collection<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Set.of();
        }
        return raw.stream()
                .map(VeilleDomainNormalizer::canonical)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
