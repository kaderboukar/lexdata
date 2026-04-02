# Mise en production : updates recommandés pour `docker-compose.yml`

## Objectif
Rendre la stack “production-ready” (sécurité, résilience, observabilité, et comportement déterministe au déploiement).

## Hypothèses
- Déploiement via `docker compose up -d` sur une VM ou un hôte unique (pas forcément Kubernetes).
- Trafic externe entrant par le `lexdata-gateway` (souvent derrière un reverse-proxy TLS).
- Les services internes (Postgres/Redis/RabbitMQ/Elastic/etc.) communiquent uniquement via le réseau Docker.

## Modifs prioritaires (must-have)
1. **Sortir tous les secrets du `docker-compose.yml`**
   - Remplacer `POSTGRES_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`, `LEXDATA_JWT_SECRET`, `EUREKA_*PASSWORD`, `RABBITMQ_DEFAULT_PASS`, etc.
   - Utiliser `secrets:` (Docker) et/ou un mécanisme externe (Vault, SSM, variables d’environnement gérées hors du repo).
   - Ne pas mettre des `user:pass@host` dans des URLs (ex: Eureka).

2. **Mettre en place des migrations DB contrôlées**
   - Ne plus utiliser `SPRING_JPA_HIBERNATE_DDL_AUTO: update` en production.
   - Préférer Flyway/Liquibase.
   - Côté Spring, viser `ddl-auto=validate` (ou `none`) et laisser les migrations être la source de vérité.

3. **Ajouter `restart` pour la résilience**
   - Pour **tous** les services : `restart: unless-stopped`.
   - En complément, vérifier que les apps gèrent correctement les redémarrages (reconnexion DB/bus/ES).

4. **Limiter strictement l’exposition réseau**
   - Ne publier des ports (`ports:`) que pour l’entrée (souvent `lexdata-gateway` et éventuellement le frontend si distinct).
   - Supprimer ou réduire l’exposition pour `postgres`, `redis`, `rabbitmq`, `elasticsearch`, `kibana`, `logstash`, `zipkin`, etc.
   - Recommandation : séparer en 2 réseaux Docker (ex: `backend-internal` en `internal: true` + un réseau “edge” pour le gateway/ingress).

5. **Mettre en place des healthchecks cohérents et un ordering de démarrage fiable**
   - Ajouter `healthcheck` pour chaque service applicatif exposant un endpoint fiable (ex: `GET /actuator/health`).
   - S’assurer que `depends_on: condition: service_healthy` ne dépend pas uniquement de Postgres/Eureka : ajouter aussi Redis/RabbitMQ/Elasticsearch quand les services en consomment.

6. **Éviter le nommage fixe des containers**
   - Retirer `container_name:` partout (meilleure compatibilité avec les redéploiements et le scaling).

## Recommandations (should-have)
1. **Versionner les images**
   - Remplacer `:latest` par des tags explicites (ou digests).
   - Exemple : `lexdata/gateway:1.2.3`.

2. **Logs & rotation**
   - Configurer la rotation (taille max, nombre de fichiers) via `logging:` (driver `json-file` par défaut) pour éviter de remplir le disque.

3. **Ressources / limites**
   - Définir des limites CPU/mémoire (notamment Elasticsearch).
   - Sur Elasticsearch : ajouter `ulimits` et réglages recommandés (ex: `memlock`, `vm.max_map_count` si besoin).

4. **TLS et politique d’accès**
   - Côté edge : HTTPS obligatoire.
   - Minimiser les services exposés, appliquer auth et/ou firewall.

5. **Stratégie de schéma applicative**
   - Vérifier que chaque service :
     - supporte le mode “DB prête mais services en retard” (retry avec backoff),
     - n’échoue pas de manière irréversible si un composant distant (ES/RabbitMQ) redémarre.

## Problèmes visibles dans ton compose actuel (à traiter en production)
- `SPRING_JPA_HIBERNATE_DDL_AUTO: update` : risque de dérive de schéma.
- Secrets en clair dans `x-service-env` (JWT, mots de passe, datasource, Eureka URL).
- Trop de ports exposés (au minimum : Postgres/Redis/RabbitMQ/Elasticsearch/Kibana/Logstash/Zipkin).
- `depends_on` pas systématique (ex: Elasticsearch/Redis/RabbitMQ pas toujours couvert en condition).
- Pas de `restart`.

## Checklist “Go Live”
1. Les secrets ne figurent plus dans le `docker-compose.yml`.
2. Les migrations DB sont appliquées avant démarrage applicatif (et `ddl-auto` est en mode safe).
3. Les seuls ports exposés correspondent à l’edge (gateway/front) et rien d’autre.
4. Tous les services applicatifs ont un `healthcheck` fiable.
5. `restart: unless-stopped` est présent partout.
6. Images taggées (pas de `latest`).
7. Journaux configurés pour rotation.
8. Elasticsearch configuré (ressources/limites) et healthcheck non fragile.

## Kubernetes (microservices) — mises a jour production-ready

### Priorite P0 (bloquant prod)
1. **Secrets et credentials**
   - Retirer tout mot de passe des ConfigMaps.
   - Mettre `EUREKA_ADMIN_PASSWORD`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_RABBITMQ_PASSWORD`, `LEXDATA_JWT_SECRET`, `LEXDATA_APP_JWTSECRET` dans un Secret (ou External Secrets/Vault).
   - Corriger `k8s/secrets.yaml` pour inclure `LEXDATA_JWT_SECRET` (actuellement manquant).

2. **Supprimer les hardcodes sensibles**
   - `k8s/configmap.yaml`: enlever `admin:admin_password` de `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`.
   - `k8s/platform/discovery.yaml`: remplacer `SPRING_SECURITY_USER_PASSWORD: admin_password` par une `secretKeyRef`.

3. **Verrouiller les versions d'images**
   - Remplacer toutes les images `:latest` par des tags versionnes (ou digests SHA).

### Priorite P1 (stabilite/resilience)
1. **Probes completes**
   - Ajouter `startupProbe` et `livenessProbe` sur tous les Deployments/StatefulSets applicatifs.
   - Garder `readinessProbe` existante.

2. **Autoscaling et disponibilite**
   - Ajouter HPA (CPU + memoire) pour gateway et microservices critiques.
   - Ajouter PodDisruptionBudget au minimum pour gateway, discovery, auth, user.

3. **Securite pod**
   - Ajouter `securityContext` (pod + container) :
   - `runAsNonRoot: true`
   - `allowPrivilegeEscalation: false`
   - `readOnlyRootFilesystem: true` (si possible)
   - `capabilities.drop: ["ALL"]`

### Priorite P2 (securite reseau/ops)
1. **NetworkPolicies**
   - Isoler namespace par defaut (deny-all) puis ouvrir uniquement les flux necessaires :
   - gateway -> services
   - services -> postgres/redis/rabbitmq/elasticsearch
   - ingress -> gateway (et eventuellement kibana/eureka si exposes)

2. **Ingress TLS**
   - Activer TLS (cert-manager + issuer) et redirection HTTPS.
   - Reduire l'exposition de Kibana/Eureka en production (auth forte ou acces prive).

3. **Base de donnees**
   - Changer `SPRING_JPA_HIBERNATE_DDL_AUTO` de `update` a `validate`.
   - Standardiser les migrations via Flyway/Liquibase.

### Fichiers K8s a mettre a jour en priorite
- `k8s/configmap.yaml`
- `k8s/secrets.yaml`
- `k8s/platform/discovery.yaml`
- `k8s/platform/gateway.yaml`
- `k8s/services/*.yaml` (probes, securityContext, image tags)
- `k8s/ingress.yaml`
- Ajouter de nouveaux manifests :
- `k8s/networkpolicy/*.yaml`
- `k8s/hpa/*.yaml`
- `k8s/pdb/*.yaml`

