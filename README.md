# Pharmacy DevSecOps

Projet de Fin d'Année (PFA) — Conception et mise en œuvre d'une chaîne DevSecOps pour l'intégration, le déploiement continu et la supervision d'une application de gestion pharmaceutique.

**Démo en ligne :**
- Frontend : https://pharmacy-frontend-2x4i.onrender.com
- Backend (health check) : https://pharmacy-backend-h2ep.onrender.com/actuator/health

## Contexte

Ce projet utilise une application de gestion pharmaceutique comme support pour mettre en place une chaîne DevSecOps complète, automatisant son cycle de vie du commit jusqu'à la supervision en production : backend Spring Boot et frontend React sont testés, scannés et publiés séparément par le même pipeline, puis déployés comme deux services indépendants.

## Architecture

```
Développeur → GitHub → GitHub Actions (CI/CD)
                            │
        ┌───────────────────┼──────────────────────┐
        ▼                   ▼                       ▼
   Backend (Maven)     Frontend (npm)          Gitleaks
   Tests + JaCoCo       npm audit             (secrets, sur tout le repo)
   SonarCloud
        │                   │
        ▼                   ▼
   Docker build        Docker build
   Trivy (backend)     Trivy (frontend)
        │                   │
        └─────────┬─────────┘
                   ▼
              Docker Hub
                   │
                   ▼
      Render (2 services distincts)
      ┌─────────────┴─────────────┐
      ▼                           ▼
  pharmacy-backend           pharmacy-frontend
      │
      ▼
Neon PostgreSQL (cloud)

Prometheus → Grafana (supervision, local via docker-compose)
```

## Stack technique

| Composant | Technologie |
|---|---|
| Backend | Spring Boot (Java 17), Spring Security |
| Frontend | React 18 + TypeScript, Vite, Tailwind CSS, React Query, React Hook Form + Zod |
| Authentification | JWT dans un cookie `HttpOnly`/`Secure`, mots de passe hachés BCrypt, RBAC serveur (rôles CLIENT / PHARMACIEN / RESPONSABLE) |
| Base de données | PostgreSQL (Neon, cloud) |
| Conteneurisation | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Tests | JUnit + JaCoCo (backend), Vitest + Testing Library (frontend) |
| Qualité de code | SonarCloud |
| Sécurité | Trivy (vulnérabilités images backend et frontend), Gitleaks (secrets), `npm audit` (dépendances frontend) |
| Registre d'images | Docker Hub (`pharmacy-backend`, `pharmacy-frontend`) |
| Hébergement | Render (2 services Web indépendants) |
| Supervision | Prometheus, Grafana (via `docker-compose`, environnement local) |

## Lancer le projet en local

Prérequis : Docker Desktop installé.

```bash
git clone https://github.com/OUMA-ch/pharmacy-devsecops.git
cd pharmacy-devsecops
```

Crée un fichier `.env` à la racine avec au minimum :
```
DB_PASSWORD=ton_mot_de_passe_neon
JWT_SECRET=une_valeur_aleatoire_d_au_moins_32_caracteres
```

Variables optionnelles (valeurs par défaut adaptées au développement local en HTTP) :
```
SECURITY_COOKIE_SECURE=false
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080,http://localhost:3000
VITE_API_BASE_URL=http://localhost:8080
```

Puis lance :
```bash
docker-compose up --build
```

- Frontend : http://localhost:5173
- Backend : http://localhost:8080
- Prometheus : http://localhost:9091
- Grafana : http://localhost:3000 (admin/admin)

## Pipeline CI/CD

Chaque push sur `develop` ou `master` déclenche automatiquement :

**Backend**
1. Détection de secrets sur tout le dépôt (Gitleaks)
2. Compilation et tests unitaires (JUnit)
3. Vérification de la couverture de code (JaCoCo)
4. Analyse de qualité (SonarCloud)
5. Construction de l'image Docker
6. Scan de vulnérabilités (Trivy — bloquant sur CRITICAL/HIGH)
7. Publication sur Docker Hub *(branche `master` uniquement, tags `:sha` et `:latest`)*

**Frontend**
8. Installation des dépendances npm (avec cache)
9. Audit des dépendances de production (`npm audit`)
10. Construction de l'image Docker (build Vite intégré, avec l'URL de l'API backend injectée au build)
11. Scan de vulnérabilités (Trivy — même politique que le backend)
12. Publication sur Docker Hub *(branche `master` uniquement, tags `:sha` et `:latest`)*

Le pipeline bloque automatiquement en cas de vulnérabilité critique/haute non documentée, ou de couverture de tests insuffisante.

## Déploiement (Render)

Backend et frontend sont déployés comme **deux services Render distincts**, chacun en mode *"Deploy an existing image"* pointant vers son image Docker Hub respective.

> ⚠️ Un service Render en mode image existante ne re-tire pas automatiquement un nouveau tag `:latest` publié par la CI. Après chaque publication réussie sur `master`, un déploiement manuel (**Manual Deploy → Deploy latest image**) est nécessaire sur chacun des deux services.

Variables d'environnement à configurer côté Render :

| Service | Variable | Valeur |
|---|---|---|
| Backend | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Identifiants Neon |
| Backend | `JWT_SECRET` | Secret aléatoire (≥ 32 caractères) |
| Backend | `CORS_ALLOWED_ORIGINS` | URL du frontend déployé |
| Backend | `SECURITY_COOKIE_SAMESITE` | `None` (frontend et backend sont deux sous-domaines `*.onrender.com` distincts) |
| Frontend (build) | `FRONTEND_API_BASE_URL` (repo variable GitHub) | URL du backend déployé, injectée au build Docker via `VITE_API_BASE_URL` |

## Sécurité

Voir [SECURITY.md](./SECURITY.md) pour le détail de la politique de sécurité du projet (Gitleaks, Trivy, SonarCloud, gestion des secrets, hachage des mots de passe, protection anti brute-force, CORS/cookies cross-site).

## Structure du projet

```
pharmacy-devsecops/
├── backend/            # API REST Spring Boot
├── frontend-react/     # Application web React (Vite + TypeScript)
├── database/           # Script SQL de la base de données
├── monitoring/         # Configuration Prometheus
├── .github/workflows/  # Pipeline CI/CD
├── docker-compose.yml
├── SECURITY.md
└── README.md
```

## Auteure

Oumaima Chihab — Master Réseaux et Systèmes Informatiques, FST Settat
