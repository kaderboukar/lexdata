# API Documentation LexData (Frontend)

Les endpoints ci-dessous sont extraits du code existant (contrôleurs `@RestController` + `WebSecurityConfig`). Les réponses/erreurs sont simplifiées pour une lecture frontend.

## 🔐 Auth Service

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/auth/login` | Authentifie l’utilisateur et renvoie access token + refresh token | Non | — | `{ "username": "...", "password": "..." }` (`LoginRequest`) | — | `JwtResponse` | 400, 401, 403, 404, 500 (compte verrouillé renvoie aussi `429` dans le code) |
| POST | `/api/auth/refreshtoken` | Réémet un access token à partir d’un refresh token | Non | — | `{ "refreshToken": "..." }` (`TokenRefreshRequest`) | — | `TokenRefreshResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/logout` | Déconnecte l’appareil (suppression refresh token) | Oui | Tous authentifiés | `{ "refreshToken": "..." }` (`TokenRefreshRequest`) | — | `MessageResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/logout-all` | Révoque toutes les sessions de l’utilisateur courant | Oui | Tous authentifiés | — | — | `MessageResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/register` | Crée un compte utilisateur (rôle standard) et déclenche vérification email | Non | — | `{ "username": "...", "email": "...", "telephone": "...", "firstName": "...", "lastName": "...", "password": "..." }` (`SignupRequest`) | — | `MessageResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/forgot-password` | Initie une réinitialisation de mot de passe (réponse générique) | Non | — | `{ "email": "..." }` (`ForgotPasswordRequest`) | — | `MessageResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/reset-password` | Change le mot de passe à partir du token | Non | — | `{ "token": "...", "newPassword": "..." }` (`ResetPasswordRequest`) | — | `MessageResponse` | 400, 401, 403, 404, 500 |
| GET | `/api/auth/verify-email?token=...` | Vérifie l’email via token | Non | — | — | `token` (string) | `MessageResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/resend-verification?email=...` | Renvoie un email de vérification | Non | — | — | `email` | `MessageResponse` | 400, 401, 403, 404, 500 |
| GET | `/api/auth/users/{id}` | Récupère un utilisateur par ID | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` (path) | `UserDto` | 400, 401, 403, 404, 500 |
| GET | `/api/auth/users` | Liste/Recherche des utilisateurs (pagination) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `search` (query, optionnel), `page/size/sort` (`Pageable`) | `Page<UserDto>` | 400, 401, 403, 404, 500 |
| PATCH | `/api/auth/users/{id}/role` | Met à jour le rôle d’un utilisateur | Oui | `SUPER_ADMIN` | `{ "role": "ROLE_AVOCAT" \| "AVOCAT" }` (Map `role`) | `id` (path) | `{"message":"..."}` | 400, 401, 403, 404, 500 |
| DELETE | `/api/auth/users/{id}` | Soft delete d’un utilisateur | Oui | `SUPER_ADMIN` | — | `id` (path) | `204 No Content` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/role-requests` | Crée une demande de changement de rôle (utilisateur courant) | Oui | Authentifié (RBAC implicite) | `{ "requestedRole": "..." }` (`RoleChangeRequestCreateRequest`) | — | `RoleChangeRequestResponse` | 400, 401, 403, 404, 500 |
| GET | `/api/auth/role-requests/admin` | Liste toutes les demandes (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | — | `List<RoleChangeRequestResponse>` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/role-requests/admin/{id}/approve` | Approuve une demande de rôle | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` (path) | `RoleChangeRequestResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/role-requests/admin/{id}/reject` | Rejette une demande de rôle | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `{ "reason": "..." }` (`RoleChangeRequestRejectRequest`) | `id` (path) | `RoleChangeRequestResponse` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/internal/user-ids/by-usernames` | Résolution interne : usernames → userIds | Non (clé interne) | Service interne | `["username1", "..."]` | — | `List<Long>` | 400, 401, 403, 404, 500 |
| POST | `/api/auth/internal/users/resolve` | Résolution interne : userIds → usernames | Non (clé interne) | Service interne | `[1,2,3]` | — | `List<IdUsernameDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/auth/internal/user-ids?role=...&page=...&size=...` | Liste interne d’IDs utilisateurs (option filtrage par rôle) | Non (clé interne) | Service interne | — | `role` (optionnel), `page`, `size` | `UserIdPageResponse` | 400, 401, 403, 404, 500 |

## 👤 User Service

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/users/me` | Récupère le profil agrégé + préférences de l’utilisateur | Oui | Tous authentifiés | — | — | `UserAggregateDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/users/me` | Met à jour le profil + préférences | Oui | Tous authentifiés | `UserUpdateRequest` (champs profil + préférences) | — | `UserAggregateDto` | 400, 401, 403, 404, 500 |
| GET | `/api/users/me/export` | Exporte les données utilisateur (RGPD) | Oui | Tous authentifiés | — | — | `UserAggregateDto` (download JSON) | 400, 401, 403, 404, 500 |
| DELETE | `/api/users/me` | Anonymisation RGPD (données personnelles + désactivation prefs) | Oui | Tous authentifiés | — | — | Texte de confirmation | 400, 401, 403, 404, 500 |
| POST | `/api/company/invite` | Invite un employé à rejoindre votre entreprise | Oui | Tous authentifiés | `{ "email": "...", "role": "ADMIN_ENTREPRISE \| UTILISATEUR_ENTREPRISE" }` | — | `CompanyMembershipDto` | 400, 401, 403, 404, 500 |
| GET | `/api/company/members` | Liste les membres de votre entreprise | Oui | Tous authentifiés | — | — | `List<CompanyMembershipDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/profiles` | Liste les profils (admin, paginé) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `page/size/sort` (`Pageable`) | `Page<UserProfileDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/profiles/{username}` | Détail d’un profil par username | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `username` | `UserProfileDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/profiles/{username}/verify` | Vérifie/valide le statut KYC d’un profil | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `KycVerificationRequest` (ex: `{ "status": "...", "comment": "..." }`) | `username` | `UserProfileDto` | 400, 401, 403, 404, 500 |
| GET | `/api/preferences` | Récupère les préférences de notification | Oui | Tous authentifiés | — | — | `UserPreferenceDto` | 400, 401, 403, 404, 500 |
| POST | `/api/preferences` | Met à jour les préférences de notification | Oui | Tous authentifiés | `{ "followedTopics": [...], "alertKeywords": [...], "emailEnabled": true/false, "pushEnabled": true/false, "smsEnabled": true/false, "timezone": "..." }` | — | `UserPreferenceDto` | 400, 401, 403, 404, 500 |
| GET | `/api/preferences/by-domain/{domain}` | Liste les usernames ayant suivi un domaine | Oui | Tous authentifiés | — | `domain` (LegalDomain) | `List<String>` | 400, 401, 403, 404, 500 |

## 📚 Juridique Base

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/juridique/textes` | Recherche de textes (optionnel : includeNonPublie, réservé aux admins) | Oui | Tous authentifiés (includeNonPublie réservé) | — | `recherche`, `domaine`, `type`, `includeNonPublie=false`, `page/size/sort` (`Pageable`) | `Page<TexteJuridiqueDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/textes/search` | Recherche avancée (includeNonPublie réservé aux admins) | Oui | Tous authentifiés (includeNonPublie réservé) | — | `q`, `includeNonPublie=false` | `List<SearchResultDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/textes/{id}` | Récupère un texte par ID (publie si non-admin) | Oui | Tous authentifiés | — | `id` | `TexteJuridiqueDto` | 400, 401, 403, 404, 500 |
| POST | `/api/juridique/textes` | Crée un texte juridique (workflow) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `TexteRequest` (ex: `{ "titre", "referenceOfficielle", "type", "domaine", "dateSignature", "contenu", ... }`) | — | `TexteJuridiqueDto` | 400, 401, 403, 404, 500 |
| PATCH | `/api/juridique/textes/{id}/status?status=...` | Met à jour le statut de publication | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id`, `status` | `TexteJuridiqueDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/juridique/textes/{id}` | Met à jour un texte juridique + envoie event (modification) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `TexteRequest` | `id` | `TexteJuridiqueDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/juridique/textes/{id}` | Soft delete d’un texte juridique | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `204 No Content` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/textes/{id}/versions` | Liste des versions d’un texte (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `List<TextVersionDto>` | 400, 401, 403, 404, 500 |
| POST | `/api/juridique/textes/{id}/versions` | Crée une version (résumé de modification) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `summary` (string body) | `id` | `TextVersionDto` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/textes/{id}/annotations` | Récupère les annotations de l’utilisateur pour un texte | Oui | Authentifié | — | `id` | `List<LegalAnnotationDto>` | 400, 401, 403, 404, 500 |
| POST | `/api/juridique/textes/{id}/annotations` | Crée une annotation sur un texte | Oui | Authentifié | `AnnotationRequest` `{ "note": "..." }` | `id` | `LegalAnnotationDto` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/textes/annotations/me` | Récupère toutes les annotations de l’utilisateur | Oui | Authentifié | — | — | `List<LegalAnnotationDto>` | 400, 401, 403, 404, 500 |
| PUT | `/api/juridique/annotations/{id}` | Met à jour une annotation (ownership par userId=JWT username) | Oui | Authentifié | `AnnotationRequest` `{ "note": "..." }` | `id` | `LegalAnnotationDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/juridique/annotations/{id}` | Supprime une annotation (ownership) | Oui | Authentifié | — | `id` | `204 No Content` | 400, 401, 403, 404, 500 |
| POST | `/api/juridique/favorites/{textId}` | Ajoute un texte aux favoris | Oui | Authentifié | — | `textId` | `FavoriteDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/juridique/favorites/{textId}` | Retire un texte des favoris | Oui | Authentifié | — | `textId` | `204 No Content` | 400, 401, 403, 404, 500 |
| GET | `/api/juridique/favorites/me` | Liste mes favoris | Oui | Authentifié | — | — | `List<FavoriteDto>` | 400, 401, 403, 404, 500 |

## 🧾 Synthèse

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| POST | `/api/synthese/admin/fiches` | Crée une fiche synthèse (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `SyntheseRequest` | — | `FicheSynthetiqueDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/synthese/admin/fiches/{id}` | Met à jour une fiche synthèse (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `SyntheseRequest` | `id` | `FicheSynthetiqueDto` | 400, 401, 403, 404, 500 |
| PATCH | `/api/synthese/admin/fiches/{id}/status?status=...` | Met à jour le statut d’une fiche (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id`, `status` | `FicheSynthetiqueDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/synthese/admin/fiches/{id}` | Supprime une fiche synthèse (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `204 No Content` | 400, 401, 403, 404, 500 |
| GET | `/api/synthese/fiches` | Liste des fiches publiées (paginer) | Oui | Authentifié | — | `page/size` | `Page<FicheSynthetiqueDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/synthese/fiches/{id}/pdf` | Télécharge le PDF d’une fiche (premium/admin selon statut) | Oui | `USER`, `JURISTE`, `AVOCAT`, `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `byte[]` (PDF) | 400, 401, 403, 404, 500 |
| GET | `/api/synthese/fiches/{id}` | Récupère une fiche par ID | Oui | `USER`, `JURISTE`, `AVOCAT`, `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `FicheSynthetiqueDto` | 400, 401, 403, 404, 500 |
| GET | `/api/synthese/fiches/texte/{texteId}` | Récupère une fiche publiée liée à un texte juridique | Oui | Authentifié | — | `texteId` | `FicheSynthetiqueDto` | 400, 401, 403, 404, 500 |
| GET | `/api/synthese/admin/fiches/{id}/versions` | Versions d’une fiche synthèse (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `List<SyntheseVersionDto>` | 400, 401, 403, 404, 500 |

## 🎯 Veille

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/veille/admin/alertes` | Liste toutes les alertes de veille (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `page/size` | `Page<AlerteVeilleDto>` | 400, 401, 403, 404, 500 |
| POST | `/api/veille/admin/alertes` | Crée une alerte de veille (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `AlerteRequest` | — | `AlerteVeilleDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/veille/admin/alertes/{id}` | Met à jour une alerte de veille (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | `AlerteRequest` | `id` | `AlerteVeilleDto` | 400, 401, 403, 404, 500 |
| PATCH | `/api/veille/admin/alertes/{id}/status?status=...` | Met à jour le statut d’une alerte ; déclenche la notification via outbox | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id`, `status` | `AlerteVeilleDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/veille/admin/alertes/{id}` | Supprime une alerte (admin) | Oui | `AGENT_ADMIN`, `SUPER_ADMIN` | — | `id` | `204 No Content` | 400, 401, 403, 404, 500 |
| GET | `/api/veille/feed?page=...&size=...` | Fil d’alertes publiées pour l’utilisateur | Oui | Authentifié | — | `page/size` | `List<AlerteVeilleDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/veille/alertes/{id}` | Récupère une alerte par ID (uniquement PUBLISHED) | Oui | Authentifié | — | `id` | `AlerteVeilleDto` | 400, 401, 403, 404, 500 |
| POST | `/api/veille/subscriptions` | Crée une subscription de veille | Oui | Authentifié | `VeilleSubscriptionRequest` `{ "domaines": [...], "textTypes": [...], "active": true/false }` | — | `VeilleSubscriptionDto` | 400, 401, 403, 404, 500 |
| PUT | `/api/veille/subscriptions/{id}` | Met à jour une subscription de veille | Oui | Authentifié | `VeilleSubscriptionRequest` | `id` | `VeilleSubscriptionDto` | 400, 401, 403, 404, 500 |
| PATCH | `/api/veille/subscriptions/{id}/active?active=...` | Active/désactive une subscription | Oui | Authentifié | — | `id`, `active` | `VeilleSubscriptionDto` | 400, 401, 403, 404, 500 |
| GET | `/api/veille/subscriptions/me` | Récupère mes subscriptions | Oui | Authentifié | — | — | `List<VeilleSubscriptionDto>` | 400, 401, 403, 404, 500 |
| GET | `/api/veille/alerts/me` | Récupère mes alertes (filtres + pagination) | Oui | Authentifié | — | `unreadOnly`, `domaine`, `type`, `fromDate`, `toDate`, `page/size` | `Page<UserAlertDto>` | 400, 401, 403, 404, 500 |
| PATCH | `/api/veille/alerts/me/{userAlertId}/read?read=...` | Marque une alerte comme lue/non lue | Oui | Authentifié | — | `userAlertId`, `read` | `UserAlertDto` | 400, 401, 403, 404, 500 |
| DELETE | `/api/veille/alerts/me/{userAlertId}` | Supprime (soft) une alerte utilisateur | Oui | Authentifié | — | `userAlertId` | `204 No Content` | 400, 401, 403, 404, 500 |

## 🔔 Notifications

| Méthode | Endpoint | Description | Auth requise | Rôles | Body (Request) | Params / Query | Réponse | Erreurs possibles |
|---|---|---|---|---|---|---|---|---|
| GET | `/api/notifications` | Liste mes notifications (filtres + pagination) | Oui | Authentifié | — | `page`, `size`, `unreadOnly`, `type`, `from`, `to` | `Page<NotificationResponse>` | 400, 401, 403, 404, 500 |
| PATCH | `/api/notifications/{id}/read` | Marque une notification comme lue | Oui | Ownership (userId) | — | `id` | `NotificationResponse` | 400, 401, 403, 404, 500 |
| PATCH | `/api/notifications/{id}/unread` | Marque une notification comme non lue | Oui | Ownership (userId) | — | `id` | `NotificationResponse` | 400, 401, 403, 404, 500 |
| DELETE | `/api/notifications/{id}` | Soft delete (suppression côté utilisateur) | Oui | Ownership (userId) | — | `id` | `204 No Content` | 400, 401, 403, 404, 500 |
| GET | `/api/notifications/preferences` | Récupère les préférences de notification | Oui | Authentifié | — | — | `NotificationPreference` | 400, 401, 403, 404, 500 |
| PUT | `/api/notifications/preferences` | Met à jour les préférences de notification | Oui | Authentifié | `NotificationPreference` (champs: emailEnabled, smsEnabled, pushEnabled, inAppEnabled, digestFrequency, veilleAlerts, syntheseAlerts, calendrierAlerts, contratAlerts, consultationAlerts, paiementAlerts, tribuneAlerts, ...) | — | `NotificationPreference` | 400, 401, 403, 404, 500 |
| POST | `/api/admin/notifications/send` | Broadcast ciblé de notifications (admin) | Oui | `SUPER_ADMIN`, `AGENT_ADMIN` | `AdminBroadcastRequest` `{ "targetType": "ALL|ROLE|USERS", "role"?, "userIds"?, "title", "message", "link"?, "channels"?, "type" }` | — | `202 Accepted` | 400, 401, 403, 404, 500 |

