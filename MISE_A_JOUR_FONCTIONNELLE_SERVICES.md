# Mise a jour fonctionnelle des services

## Objectif
Centraliser les modifications fonctionnelles a appliquer sur les microservices `gateway`, `auth-service` et `user-service` pour aligner le produit, fiabiliser les parcours utilisateur et simplifier l'exploitation.

## Regles de priorisation
- **P0** : indispensable avant generalisation en production.
- **P1** : fortement recommande pour robustesse et maintenabilite.
- **P2** : evolution produit / optimisation.

---

## Gateway

### P0
1. **Unifier la politique CORS**
   - Garder une seule source de verite (gateway), configurable par variable d'environnement.
   - Definir explicitement les origines autorisees selon environnement (dev/staging/prod).

2. **Durcir l'exposition des endpoints techniques**
   - Limiter les endpoints actuator publics a `health` et `info`.
   - Verifier que les endpoints sensibles (metrics/gateway details) ne sont pas exposes publiquement.

3. **Stabiliser les routes publiques/privees**
   - Verifier la liste des routes `permitAll` pour eviter des ouvertures involontaires.
   - Ajouter des tests de non-regression sur les routes protegees.

### P1
1. **Rate limiting par criticite d'endpoint**
   - Renforcer `login`, `forgot-password`, `refreshtoken`, endpoints admin.
   - Ajuster les seuils par type de route (publique vs privee).

2. **Contractualiser les fallbacks**
   - Definir des reponses standard de fallback (code, message, traceId).
   - Ajouter un fallback uniforme pour indisponibilite d'un service downstream.

3. **Tracing et logs metier**
   - Propager un `traceId` sur toutes les reponses d'erreur.
   - Ajouter des logs structurees par route (latence, status, target service).

### P2
1. **Versioning API**
   - Introduire progressivement `/api/v1/...` pour faciliter les evolutions sans rupture front.

2. **Politique anti-abus avancee**
   - Ajouter blocage progressif (burst control + cooldown) sur endpoints critiques.

---

## Auth-Service

### P0
1. **Conserver l'inscription publique en role utilisateur uniquement**
   - Confirmer que `register` n'accepte aucun role privilegie en entree.
   - Documenter que l'elevation de role passe par un workflow admin separe.

2. **Uniformiser la gestion d'erreurs**
   - Remplacer les `try/catch` repetitifs dans les endpoints par le `GlobalExceptionHandler`.
   - Standardiser les messages d'erreur renvoyes au client.

3. **Verifier la coherence des secrets obligatoires**
   - S'assurer que toutes les variables sensibles sont injectees en environnement (JWT, DB, Rabbit, Eureka).

### P1
1. **Workflow refresh token complet**
   - Conserver la rotation a chaque refresh.
   - Ajouter tests automatiques sur invalidation de l'ancien token.

2. **Hardening brute-force**
   - Completer la strategie IP + username avec politique de confiance proxy.
   - Ajouter metriques pour supervision (taux de lock, tentatives echouees).

3. **Contrat d'erreur API commun**
   - Retourner un schema standard: `code`, `message`, `timestamp`, `traceId`.

### P2
1. **MFA pour comptes sensibles**
   - Activer MFA pour roles admin et operations critiques.

2. **Audit securite enrichi**
   - Journaliser les evenements de securite majeurs (refresh suspect, reset, locks repetes).

---

## User-Service

### P0
1. **Unifier les parcours profil**
   - Eviter le chevauchement entre `/api/users/me` et `/api/profiles/me`.
   - Choisir une facade principale et deprecier l'autre progressivement.

2. **Harmoniser les erreurs metier**
   - Generaliser le `GlobalExceptionHandler` sans retours hétérogenes selon endpoint.
   - Supprimer les exceptions generiques dans les controllers.

3. **Verrouiller les operations sensibles par role**
   - Revalider les regles `@PreAuthorize` sur KYC et gestion entreprise.

### P1
1. **Completer le workflow entreprise**
   - Ajouter acceptation/refus d'invitation, expiration, suppression membre, idempotence.

2. **Completer le workflow KYC**
   - Ajouter historique de statut, motifs de rejet, re-soumission, audit.

3. **Durcir l'exposition des endpoints de listing**
   - Encadrer pagination/filtrage pour limiter les requetes couteuses.

### P2
1. **Centre de preferences avance**
   - Granularite des notifications par canal et type d'evenement.

2. **Export RGPD evolue**
   - Ajouter export plus complet (horodatage, metadonnees, format versionne).

---

## Taches transverses (3 services)

### P0
1. **Schema de reponse d'erreur commun**
2. **Checklist de non-regression securite**
3. **Tests d'integration API critiques (login, refresh, profil, KYC)**

### P1
1. **Contrats d'evenements versionnes (RabbitMQ)**
2. **DLQ + retry backoff sur consumers**
3. **Dashboards d'observabilite metier**

### P2
1. **Roadmap d'evolution API (v1 -> v2)**
2. **Catalogue de capacites produit par domaine (auth, profil, entreprise, KYC)**

