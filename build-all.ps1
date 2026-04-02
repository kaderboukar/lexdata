# =============================================================================
# build-all.ps1 — Script de build Maven pour tous les microservices LexData
# Lance mvn clean package -DskipTests sur chaque microservice
# =============================================================================
# Usage : .\build-all.ps1
# =============================================================================

$ErrorActionPreference = "Stop"

$services = @(
    "lexdata-discovery-service",
    "lexdata-gateway",
    "lexdata-auth-service",
    "lexdata-user-service",
    "lexdata-juridique-base",
    "lexdata-annuaire",
    "lexdata-veille",
    "lexdata-synthese",
    "lexdata-tribune",
    "lexdata-contrats",
    "lexdata-calendrier",
    "lexdata-consultations",
    "lexdata-paiements",
    "lexdata-notifications",
    "lexdata-admin",
    "lexdata-monitoring"
)

$root = $PSScriptRoot
$success = @()
$failed  = @()

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   LexData — Build de tous les microservices" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    $path = Join-Path $root $service
    if (-not (Test-Path $path)) {
        Write-Warning "Dossier introuvable : $service — ignoré"
        continue
    }

    Write-Host "➜ Building $service..." -ForegroundColor Yellow
    Push-Location $path
    try {
        & mvn clean package -DskipTests -q
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✔ $service — OK" -ForegroundColor Green
            $success += $service
        } else {
            Write-Host "  ✘ $service — ÉCHEC (exit code $LASTEXITCODE)" -ForegroundColor Red
            $failed += $service
        }
    } catch {
        Write-Host "  ✘ $service — ERREUR: $_" -ForegroundColor Red
        $failed += $service
    } finally {
        Pop-Location
    }
    Write-Host ""
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "   Résumé" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  ✔ Succès  : $($success.Count)" -ForegroundColor Green
Write-Host "  ✘ Échecs  : $($failed.Count)" -ForegroundColor Red

if ($failed.Count -gt 0) {
    Write-Host ""
    Write-Host "Services en échec :" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    exit 1
}

Write-Host ""
Write-Host "Tous les services buildés ! Lancez : docker-compose up --build -d" -ForegroundColor Green
Write-Host ""
