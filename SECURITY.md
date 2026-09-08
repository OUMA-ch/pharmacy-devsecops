# Politique de sécurité — Pharmacy DevSecOps

## Contexte

Ce projet applique une démarche DevSecOps : la sécurité est intégrée directement dans le pipeline CI/CD, pas ajoutée après coup. Chaque modification du code est automatiquement analysée avant d'être publiée.

## Contrôles de sécurité automatisés

### 1. Détection de secrets (Gitleaks)

Chaque push déclenche un scan de l'historique Git à la recherche de secrets accidentellement commités (mots de passe, tokens, clés API).

**Incident réel rencontré et résolu pendant le projet :** lors de la mise en place de Gitleaks, un mot de passe de base de données a été détecté en clair dans `docker-compose.yml` (commit historique). Ce mot de passe a été immédiatement révoqué et régénéré sur Neon (fournisseur de la base de données), puis la configuration a été corrigée pour utiliser un fichier `.env` non commité (`DB_PASSWORD=${DB_PASSWORD}`). L'entrée historique, désormais sans valeur puisque le secret est révoqué, est documentée dans `.gitleaksignore`.

### 2. Analyse des vulnérabilités (Trivy)

Chaque image Docker construite par le pipeline est scannée avec [Trivy](https://trivy.dev) à la recherche de vulnérabilités connues (CVE) dans :
- les dépendances Java (via Maven)
- les paquets système de l'image de base (Ubuntu)

**Politique de blocage :**

| Sévérité | Action |
|----------|--------|
| CRITICAL | Bloque le pipeline |
| HIGH | Bloque le pipeline |
| MEDIUM | Signalé, non bloquant |
| LOW | Signalé, non bloquant |

### 3. Gestion des exceptions (`.trivyignore`)

Certaines vulnérabilités peuvent être temporairement ignorées si :
- un correctif n'est pas encore disponible dans le BOM Spring Boot utilisé par le projet
- le risque réel est jugé faible (ex : nécessite des conditions d'exploitation non applicables à ce contexte)

Chaque exception est documentée dans `backend/.trivyignore` avec l'identifiant CVE et la justification. Ces exceptions sont réévaluées à chaque mise à jour majeure des dépendances.

**Historique de réduction des vulnérabilités** au fil du développement, grâce à :
- la mise à jour de Spring Boot (4.0.0 → 4.0.6)
- la surcharge de versions spécifiques (Tomcat, Spring Framework)
- la mise à jour du driver PostgreSQL
- la mise à jour des paquets système de l'image Docker de base

### 4. Analyse de la qualité du code (SonarCloud)

Le pipeline exécute une analyse statique du code via SonarCloud à chaque exécution, incluant la détection de bugs potentiels, code smells et vulnérabilités applicatives (SAST).

### 5. Couverture de tests (JaCoCo)

Un seuil minimum de couverture de code est vérifié à chaque build. Le pipeline échoue si ce seuil n'est pas atteint.

## Gestion des secrets

Aucune information sensible (mots de passe, tokens, clés API) n'est stockée en clair dans le dépôt Git. Toutes les valeurs sensibles sont gérées via **GitHub Secrets** :

| Secret | Usage |
|--------|-------|
| `DOCKERHUB_USERNAME` | Authentification Docker Hub |
| `DOCKERHUB_TOKEN` | Authentification Docker Hub |
| `SONAR_TOKEN` | Authentification SonarCloud |

En local et en production, les identifiants de connexion à la base de données sont injectés via des variables d'environnement (fichier `.env` non commité en local, variables d'environnement Render en production), jamais commités dans le code source.

## Connexion à la base de données

La connexion à la base de données PostgreSQL (Neon) utilise une connexion chiffrée (SSL/TLS) en environnement de production (`DB_SSLMODE=require`). Le mode SSL est désactivé uniquement dans l'environnement de test CI isolé (`DB_SSLMODE=disable`), où une base PostgreSQL temporaire sans données réelles est utilisée.

## Signaler une vulnérabilité

Ce projet étant un travail académique (PFA), il n'y a pas de canal de signalement formel. Pour toute question sur la sécurité de ce projet, contacter l'auteure via le dépôt GitHub.
