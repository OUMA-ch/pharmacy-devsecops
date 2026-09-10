# Sécurité — Frontend PharmaHOSS

Ce document liste les choix de sécurité pris dans ce frontend, leurs compromis
assumés, et les points qui restent à valider ou corriger côté backend. Rédigé dans le
cadre d'un projet DevSecOps : la transparence sur les limites compte plus que
l'apparence de conformité.

## 1. Authentification — compromis majeur, à lire en premier

**Constat backend (vérifié dans le code, non modifiable dans le cadre de cette
mission)** : `AuthController.login()` / `AuthService` ne délivrent **aucun token**. La
réponse de connexion est `{ id, nom, email, role }` — un simple résultat de
vérification d'identifiants, sans session serveur ni JWT. `SecurityConfig.java`
autorise toutes les requêtes sans authentification (`permitAll()`), et
`WebConfig.java` accepte le CORS depuis `*` sans restriction.

**Conséquence directe** : l'exigence "cookie HttpOnly, Secure, SameSite=Strict posé
par le backend" est **techniquement impossible** sans modifier le backend. Ce
frontend applique donc le repli explicitement documenté et accepté :

- Le résultat de connexion est stocké dans `sessionStorage` (pas `localStorage`) sous
  la clé `pharmahoss.user` — voir `src/auth/storage.ts`, commentaire en tête de
  fichier expliquant ce choix. `sessionStorage` limite la fenêtre d'exposition (la
  session ne survit pas à la fermeture de l'onglet) mais **ne protège pas contre le
  vol par XSS** pendant que l'onglet est ouvert — un cookie `HttpOnly` serait
  strictement supérieur.
- **Le RBAC (`ProtectedRoute`) est une protection frontend uniquement.** Il empêche un
  utilisateur normal de naviguer vers une page non autorisée dans l'UI, mais
  **n'empêche en rien un appel direct à l'API** (`curl`, Postman, etc.) : le backend
  n'impose aucune autorisation par rôle sur ses endpoints REST. Un client authentifié
  côté frontend comme "CLIENT" peut, en théorie, appeler `POST /produits` directement
  contre l'API — rien ne l'en empêche côté serveur.
- **Recommandation pour la suite** (hors périmètre de cette mission) : le backend
  devrait a minima (1) émettre un token de session (JWT ou opaque) à la connexion, (2)
  vérifier ce token sur chaque endpoint protégé, (3) appliquer une autorisation par
  rôle côté serveur (Spring Security `@PreAuthorize` ou équivalent), (4) restreindre le
  CORS à l'origine réelle du frontend plutôt que `*`.

## 2. Protection XSS

- Aucun usage de `dangerouslySetInnerHTML` dans tout le code (vérifiable : absent du
  projet). React échappe par défaut tout contenu textuel injecté via JSX (`{variable}`),
  ce qui couvre l'affichage des données utilisateur (noms de produits, messages de
  notification, etc.).

## 3. Validation des données

- Validation côté client via Zod (`src/lib/validationSchemas.ts`) sur tous les
  formulaires : login, inscription, produit, vente, ordonnance, commande, fournisseur,
  fourniture, pharmacien.
- Cette validation est un confort UX, **pas une garantie de sécurité** : le backend
  applique ses propres règles (contraintes JPA, vérifications dans les services) et
  reste la seule source de vérité pour l'intégrité des données. Le frontend ne fait
  jamais l'hypothèse que la validation côté client suffit.

## 4. Gestion des erreurs API — pas de fuite d'information technique

`src/api/client.ts` (`toSafeUserMessage`) filtre systématiquement les réponses
d'erreur du backend :

- Les messages métier explicites du backend (ex: "Impossible de supprimer ce produit :
  il a déjà été vendu.") sont affichés tels quels — ils sont sûrs et utiles.
- Les messages qui ressemblent à une trace technique (mots-clés `constraint`,
  `violat`, `foreign key`, `sql`, `hibernate`, `nested exception` — typiquement une
  `DataIntegrityViolationException` Hibernate non interceptée par le backend, ex. lors
  de la suppression d'un fournisseur encore référencé) sont **remplacés par un message
  générique**.
- Le détail technique brut est loggé uniquement en `console.error` (visible en
  développement / outils navigateur), jamais affiché à l'utilisateur final.
- Les erreurs réseau (backend injoignable) affichent un message générique, jamais la
  stack JS brute.

## 5. Secrets et configuration

- Aucun secret, clé API ou identifiant en dur dans le code source.
- L'URL de l'API est injectée via `import.meta.env.VITE_API_BASE_URL` (`.env`, voir
  `.env.example`). **Attention** : Vite intègre ces variables dans le bundle JS au
  moment du build — ce ne sont donc jamais des secrets côté client, seulement une
  configuration d'URL publique. Aucune variable sensible n'est destinée à passer par
  ce mécanisme.

## 6. CSP-friendly

- Aucun script chargé depuis un CDN externe. Toutes les dépendances (React, Recharts,
  React Query, etc.) sont installées via npm et bundlées par Vite dans les fichiers
  JS servis par nginx.
- Aucun script inline dans `index.html`.
- `nginx.conf` ajoute des en-têtes de durcissement de base (`X-Content-Type-Options`,
  `X-Frame-Options`, `Referrer-Policy`). Une politique CSP explicite (`Content-Security-Policy`)
  n'a pas été ajoutée dans ce livrable — c'est une amélioration recommandée à ajouter
  au niveau du reverse-proxy de production, une fois l'origine finale du backend
  connue (nécessaire pour renseigner `connect-src`).

## 7. Dépendances npm — vulnérabilités connues et acceptées

`npm audit` (au moment de la livraison) signale 7 vulnérabilités, **toutes dans des
dépendances de build/test, jamais dans le bundle de production**, à une exception
près :

| Paquet                                    | Sévérité           | Nature                                                                                                                                                                                                                                    | Décision                                                                                                                                                                                                                                                                                                                                                                                                 |
| ----------------------------------------- | ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `vite` (5.4.21)                           | Haute              | 3 CVE affectant uniquement le **serveur de dev** (`vite dev`) : lecture de fichiers via `.map`, contournement Windows de `server.fs.deny`, divulgation de hash NTLMv2 via UNC. N'affecte jamais `vite build` / le build statique déployé. | Conservé sur la branche 5.x (la 6.x casse la compatibilité de types avec `vitest` 2.x installé). Correctif réel seulement en 6.4.3+/7+/8+. À réévaluer lors d'une prochaine mise à jour majeure conjointe vite+vitest.                                                                                                                                                                                   |
| `vitest` (2.1.9) + `@vitest/mocker`       | Critique / Modérée | Lecture/exécution arbitraire de fichier **via l'UI de Vitest** (`vitest --ui`), jamais utilisée dans ce projet (ni en CI, ni en dev). Dépendance de test uniquement, jamais shippée.                                                      | Conservé en 2.x. Le correctif nécessite Vitest 4.1.11+/5.x, un saut majeur non validé dans le temps imparti.                                                                                                                                                                                                                                                                                             |
| `esbuild` (via vite)                      | Modérée            | Le serveur de dev esbuild répond à des requêtes cross-origin. Dev-only.                                                                                                                                                                   | Idem, lié au choix vite ci-dessus.                                                                                                                                                                                                                                                                                                                                                                       |
| `react-router-dom` (6.30.6, dernière 6.x) | Modérée            | Open redirect via backslash dans `<Link>`/`useNavigate` (CVE-2025-68470 bypass) — **dépendance d'exécution, expédiée en production.** Le correctif n'existe que sur React Router **v7** (`7.18.3+`), une migration majeure d'API.         | Mis à jour à la dernière version 6.x disponible (aucun correctif 6.x n'existe). Risque résiduel jugé faible ici : aucun `<Link to=...>` ni `navigate(...)` de ce projet ne construit sa cible à partir d'une entrée utilisateur non validée — toutes les cibles de navigation sont des chemins fixes du code. **Recommandation** : planifier une migration vers React Router v7 pour éliminer ce résidu. |

Aucune de ces vulnérabilités n'a été ignorée silencieusement : chacune est documentée
ici avec sa portée réelle (dev-only vs production) et la raison de ne pas forcer une
mise à jour majeure non validée dans le cadre de cette livraison.

## 8. Points restants à valider côté backend (hors périmètre de cette mission)

- Émission d'un vrai token de session et vérification serveur (voir section 1).
- Autorisation par rôle appliquée côté serveur, pas seulement côté UI.
- Restriction du CORS (`WebConfig.java`) à l'origine réelle du frontend plutôt que `*`.
- Message métier dédié pour l'échec de suppression d'un fournisseur référencé (à
  l'image de ce qui existe déjà pour les produits dans `ProduitService`), pour éviter
  de dépendre d'une détection heuristique côté frontend.
- Rate limiting sur `/auth/login` (aucune protection contre le brute-force constatée).
- Si des cookies d'authentification sont introduits un jour : le CORS `allowedOrigins("*")`
  actuel devra être remplacé par une liste d'origines explicite, incompatible par
  nature avec `allowCredentials(true)`.
