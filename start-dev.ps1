# =============================================================================
# start-dev.ps1 — Script de démarrage rapide (mode développement)
# Lance uniquement l'infrastructure Docker (Postgres + Redis)
# Les microservices sont lancés localement depuis l'IDE
# =============================================================================
# Usage : .\start-dev.ps1

param(
    [switch]$Stop,    # Pour arrêter l'infra : .\start-dev.ps1 -Stop
    [switch]$Full     # Pour tout démarrer (incluant microservices Docker) : .\start-dev.ps1 -Full
)

$ErrorActionPreference = "Continue"

if ($Stop) {
    Write-Host "Arrêt de la stack LexData..." -ForegroundColor Yellow
    docker-compose down --remove-orphans
    Write-Host "Stack arrêtée." -ForegroundColor Green
    exit 0
}

if ($Full) {
    Write-Host ""
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host "  LexData — Démarrage COMPLET (tous les services)" -ForegroundColor Cyan
    Write-Host "=====================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "→ Démarrage de tous les services..." -ForegroundColor Yellow
    docker-compose up --build -d
    Write-Host ""
    Write-Host "✔ Stack complète démarrée !" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Frontend       →  http://localhost:80" -ForegroundColor Cyan
    Write-Host "  Gateway        →  http://localhost:8081" -ForegroundColor Cyan
    Write-Host "  Eureka UI      →  http://localhost:8761" -ForegroundColor Cyan
    Write-Host "  Prometheus     →  http://localhost:9015/actuator/prometheus" -ForegroundColor Cyan
    Write-Host ""
    exit 0
}

# Mode développement par défaut : juste l'infra
Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  LexData — Mode Développement (infra seulement)" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "→ Démarrage de l'infrastructure (Postgres + Redis)..." -ForegroundColor Yellow
docker-compose up -d lexdata-postgres lexdata-redis

Write-Host ""
Write-Host "✔ Infrastructure prête !" -ForegroundColor Green
Write-Host ""
Write-Host "  PostgreSQL  →  localhost:5431" -ForegroundColor Cyan
Write-Host "  Redis       →  localhost:6379" -ForegroundColor Cyan
Write-Host ""
Write-Host "Démarrez maintenant vos microservices depuis votre IDE." -ForegroundColor Gray
Write-Host "Pour tout stopper : .\start-dev.ps1 -Stop" -ForegroundColor Gray
Write-Host ""
