# Test local : updates recommandés pour `docker-compose.yml`

## Objectif
Améliorer l’expérience de dev/test local sans casser la stack, tout en augmentant la “parité” avec la production (surtout sur readiness, sécurité réseau minimale et cohérence des dépendances).

## Hypothèses
- Le but est de tester sur la machine (ton PC) avec un minimum de friction.
- Tu veux garder la possibilité d’exposer certains outils (Kibana) en local.

## Ajustements “safe” (priorité 1)
1. **Centraliser les variables dans un `.env.local`**
   - Garder des valeurs simples en local, mais pas en dur dans le compose.
   - Remplacer la mécanique actuelle par `env_file: .env.local` ou substitution `${VAR}`.
   - Exemple de variables à mettre dans `.env.local` : passwords, JWT secret, etc.

2. **Ajouter `restart` (confort local)**
   - `restart: unless-stopped` sur les services.
   - Ça évite de tout relancer après une mise à jour Docker / reboot machine.

3. **Healthchecks sur les services applicatifs**
   - Si `depends_on` utilise `condition: service_healthy`, alors il faut des healthchecks sur les services applicatifs consommateurs.
   - Côté local, ça réduit les erreurs “connexion refusée” au démarrage.

4. **Mettre un minimum de retry applicatif**
   - S’assurer côté Spring que les connexions DB/Redis/RabbitMQ/ES ont un retry/backoff (si ce n’est pas déjà le cas).

## Ajustements “parité prod” (priorité 2)
1. **Retirer `container_name`**
   - Même en local, ça augmente la flexibilité (et évite les collisions si tu lances une 2e instance).

2. **Limiter les ports publiés**
   - En local, garder par exemple :
     - `8081` (gateway) et `80` (frontend),
     - éventuellement `5431` (Postgres) si tu utilises un outil externe (DBeaver),
     - éventuellement `5601` (Kibana) si nécessaire.
   - Les autres (Elastic, RabbitMQ, Redis) peuvent rester “non exposés” tout en étant accessibles via le réseau Docker.

3. **Ajouter des `profiles` optionnels**
   - Par exemple :
     - `observability` : zipkin/kibana/logstash
     - `search` : elasticsearch
     - `messaging` : rabbitmq
   - Tu peux ainsi démarrer une stack “minimale” plus rapide.

## Sur `SPRING_JPA_HIBERNATE_DDL_AUTO` en local
- En local, `update` est généralement ok pour itérer vite.
- Alternative recommandée selon ton usage :
  - si tu veux repartir proprement à chaque test : `create-drop` (sur DB jetable) ou un script de reset,
  - sinon : `update` + migrations pour apprendre la cohérence.

## Checklist “Local prêt”
1. Un fichier `.env.local` existe et le compose n’a plus les secrets en dur.
2. Le démarrage est stable : pas de crash “connexion refusée” au premier lancement.
3. Tu peux accéder au gateway/front via les ports attendus.
4. Les dépendances vitales (DB + discovery + (redis/rabbitmq/es si utilisées)) sont réellement prêtes (healthcheck).
5. Tu peux activer/désactiver Kibana/Zipkin facilement via profils si tu veux accélérer.

