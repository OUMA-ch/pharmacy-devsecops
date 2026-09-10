# Sécurité — Frontend PharmaHOSS

Ce document liste les choix de sécurité pris dans ce frontend, leurs compromis
assumés, et les points qui restent à valider ou corriger côté backend. Rédigé dans le
cadre d'un projet DevSecOps : la transparence sur les limites compte plus que
l'apparence de conformité.

## 1. Authentification — RBAC appliqué côté serveur (mis à jour)

**Remédiation appliquée (backend + frontend)** : le compromis "pas de token" décrit
initialement dans ce document est résolu. `AuthController.login()` délivre désormais
un **JWT signé** (HMAC-SHA256, `backend/.../security/JwtService.java`), transporté
dans un cookie `access_token` avec les attributs `HttpOnly`, `SameSite=Strict`, et
`Secure` (activable via `security.cookie.secure`, `true` par défaut). Chaque requête
protégée est vérifiée par `JwtAuthFilter`, et **`SecurityConfig.java` applique le RBAC
par rôle sur chaque endpoint** (`hasRole`/`hasAnyRole` selon les vraies routes
`/produits`, `/ventes`, `/commandes`, `/fournisseurs`, `/fournitures`, `/ordonnances`,
`/pharmaciens`, `/reports`, `/notifications`) — **le backend est la seule source de
vérité pour l'autorisation**, plus seulement l'UI.

Vérifié en conditions réelles (backend démarré, appels `curl`) : `POST /auth/login`
pose bien le cookie ; un appel sans cookie à une route protégée renvoie `401` ; un
compte `CLIENT` authentifié qui appelle `/produits` ou `/pharmaciens` reçoit `403` ;
un token corrompu renvoie un `401` propre (pas de `500`) ; le préflight CORS
(`OPTIONS`) fonctionne pour une origine autorisée et est refusé pour une origine
inconnue ; `/auth/logout` invalide bien le cookie.

Côté frontend (`src/api/client.ts`) : `credentials: "include"` sur chaque requête
pour que le cookie parte avec les appels cross-origin, et un intercepteur global sur
les réponses `401` qui nettoie l'affichage local et renvoie vers `/login` (sauf pour
`/auth/login` lui-même, pour ne pas transformer un simple mot de passe incorrect en
redirection). `sessionStorage` (`src/auth/storage.ts`) ne contient plus la moindre
ambiguïté : uniquement `{ id, nom, email, role }` pour l'affichage, jamais le secret
d'authentification (qui est dans le cookie, invisible en JS). `ProtectedRoute` reste
une garde de route, mais son rôle est maintenant clairement UX (éviter un flash de
contenu interdit) et non plus le seul rempart.

**Ce qui reste un choix assumé, pas un oubli :**

- **IDOR résiduel sur les ressources scoped-client** : `GET /notifications/client/{id}`,
  `/ventes/client/{id}`, `/ordonnances/client/{id}` vérifient le **rôle** (`CLIENT` a le
  droit d'appeler `/notifications/**`) mais pas que `{id}` correspond bien au `uid` du
  JWT appelant. Un CLIENT authentifié peut donc, en théorie, lire les notifications
  d'un autre client en changeant l'ID dans l'URL. Ce n'est pas ce qui était demandé
  dans le plan de remédiation (RBAC par rôle, "seule source de vérité" au niveau de la
  liste d'autorisation par route) ; corriger ceci demanderait une vérification
  d'appartenance par endpoint (comparer `claims.get("uid")` à l'ID du chemin), non
  implémentée ici pour rester dans le périmètre demandé. **Recommandation forte** de
  traiter ce point ensuite.
- **Mots de passe en clair en base** (`AuthService.login` : comparaison directe
  `user.getPassword().equals(...)`, confirmé aussi dans `PharmacienCrudService`).
  Non traité dans cette remédiation : migrer vers `BCryptPasswordEncoder` nécessite de
  rehacher les mots de passe existants en base (migration de données), ce qui est un
  chantier distinct du JWT/RBAC demandé ici. À planifier en priorité juste après.
- **Refresh token** : non implémenté. Le JWT expire après 1h (`security.jwt.expiration-ms`)
  et l'utilisateur doit se reconnecter — acceptable pour ce périmètre, mais une vraie
  UX de production voudrait un refresh token à rotation (cookie séparé).
- **Rate limiting** sur `/auth/login` : toujours absent (aucune protection anti
  brute-force). À ajouter (ex. bucket4j, ou au niveau reverse-proxy).

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

## 8. Points restants à valider côté backend

Résolu par la remédiation JWT/RBAC (section 1) : émission d'un vrai token de session,
autorisation par rôle appliquée côté serveur, CORS restreint à une liste d'origines
explicite avec `allowCredentials(true)` (`app.cors.allowed-origins`, voir
`backend/.../WebConfig.java`).

Reste ouvert :

- IDOR résiduel sur les ressources scoped-client (voir section 1) — vérification
  d'appartenance par endpoint (`uid` du JWT vs ID du chemin) non implémentée.
- Mots de passe stockés en clair en base — migration `BCryptPasswordEncoder` avec
  rehachage des comptes existants, non traitée ici (chantier distinct).
- Refresh token / durée de session glissante — actuellement expiration fixe 1h.
- Message métier dédié pour l'échec de suppression d'un fournisseur référencé (à
  l'image de ce qui existe déjà pour les produits dans `ProduitService`), pour éviter
  de dépendre d'une détection heuristique côté frontend.
- Rate limiting sur `/auth/login` (aucune protection contre le brute-force constatée).
- CSRF : `SameSite=Strict` + absence de formulaire HTML classique réduit déjà
  fortement le risque, mais si une requête `GET` produisait un jour un effet de bord,
  ajouter un token CSRF applicatif dédié.
