# Pharmacy DevSecOps

Projet de Fin d'Année (PFA) — Conception et mise en œuvre d'une chaîne DevSecOps pour l'intégration, le déploiement continu et la supervision d'une application de gestion pharmaceutique.

**Démo en ligne :** https://pharmacy-backend-h2ep.onrender.com/actuator/health

## Contexte

Ce projet utilise une application de gestion pharmaceutique existante comme support pour mettre en place une chaîne DevSecOps complète, automatisant son cycle de vie du commit jusqu'à la supervision en production.

## Architecture

```
Développeur → GitHub → GitHub Actions (CI/CD)
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
   Tests (JUnit)       SonarCloud            Trivy + Gitleaks
     + JaCoCo          (qualité)             (sécurité)
        │                                          │
        └──────────────────┬───────────────────────┘
                            ▼
                       Docker Hub
                            │
                            ▼
                    Render (déploiement)
                            │
                            ▼
                  Neon PostgreSQL (cloud)
                            │
                            ▼
              Prometheus → Grafana (supervision)
```

## Stack technique

| Composant | Technologie |
|---|---|
| Backend | Spring Boot 4 (Java 17) |
| Frontend | JavaFX (application desktop) |
| Base de données | PostgreSQL (Neon, cloud) |
| Conteneurisation | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Tests | JUnit, JaCoCo |
| Qualité de code | SonarCloud |
| Sécurité | Trivy (vulnérabilités), Gitleaks (secrets) |
| Registre d'images | Docker Hub |
| Hébergement | Render |
| Supervision | Prometheus, Grafana |

## Lancer le projet en local

Prérequis : Docker Desktop installé.

```bash
git clone https://github.com/OUMA-ch/pharmacy-devsecops.git
cd pharmacy-devsecops
```

Crée un fichier `.env` à la racine avec :
```
DB_PASSWORD=ton_mot_de_passe_neon
```

Puis lance :
```bash
docker-compose up --build
```

- Backend : http://localhost:8080
- Prometheus : http://localhost:9091
- Grafana : http://localhost:3000 (admin/admin)

## Pipeline CI/CD

Chaque push sur `develop` ou `master` déclenche automatiquement :

1. Détection de secrets (Gitleaks)
2. Compilation et tests unitaires (JUnit)
3. Vérification de la couverture de code (JaCoCo)
4. Analyse de qualité (SonarCloud)
5. Construction de l'image Docker
6. Scan de vulnérabilités (Trivy)
7. Publication sur Docker Hub *(branche `master` uniquement)*

Le pipeline bloque automatiquement en cas de vulnérabilité critique/haute non documentée, ou de couverture de tests insuffisante.

## Sécurité

Voir [SECURITY.md](./SECURITY.md) pour le détail de la politique de sécurité du projet.

## Structure du projet

```
pharmacy-devsecops/
├── backend/          # API REST Spring Boot
├── frontend/         # Application desktop JavaFX
├── database/         # Script SQL de la base de données
├── monitoring/        # Configuration Prometheus
├── .github/workflows/ # Pipeline CI/CD
├── docker-compose.yml
└── SECURITY.md
```

## Auteure

Oumaima Chihab — Master Réseaux et Systèmes Informatiques, FST Settat
