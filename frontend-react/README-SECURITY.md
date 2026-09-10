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

## 1bis. Mots de passe BCrypt + correctif IDOR (mis à jour)

Les deux points ouverts de la section précédente sont désormais résolus :

- **Mots de passe hachés (BCrypt)** : `SecurityConfig` expose un bean
  `PasswordEncoder` (`BCryptPasswordEncoder`). `AuthService.login()` vérifie via
  `passwordEncoder.matches(...)` (plus de comparaison en clair) ; `UserService.registerClient()`
  et `PharmacienCrudService.create()/update()` hachent le mot de passe avant sauvegarde.
  **Migration des comptes existants** : `security/PasswordMigrationRunner.java`
  (`ApplicationRunner`) parcourt tous les `User` au démarrage et rehache en place tout
  mot de passe qui n'a pas déjà le format BCrypt (`$2a$/$2b$/$2y$...`) — le mot de passe
  reste le même pour l'utilisateur, seul son stockage change. Idempotent (sans effet aux
  démarrages suivants une fois tous les comptes migrés). **Validé en conditions réelles**
  sur la base Neon : migration exécutée (log "N compte(s) migré(s) vers BCrypt"), login
  toujours fonctionnel avec l'ancien mot de passe en clair, mauvais mot de passe toujours
  rejeté (401).
- **Fuite du mot de passe dans les réponses API corrigée en même temps** : `POST /users/register`
  et les endpoints `/pharmaciens` renvoient l'entité directement (pas de DTO de sortie
  dédié) et incluaient donc le mot de passe (en clair, puis le hash BCrypt) dans le JSON
  de réponse. `User.password` porte maintenant `@JsonProperty(access = WRITE_ONLY)` :
  toujours accepté en entrée, plus jamais renvoyé en sortie. Vérifié : la réponse
  d'inscription ne contient plus la clé `password`.
- **IDOR sur `/notifications/client/{id}` corrigé** : `JwtAuthFilter` peuple un principal
  `AuthenticatedUser(uid, email, role)` à partir du claim `uid` du JWT.
  `NotificationController` porte `@PreAuthorize("hasAnyRole('PHARMACIEN','RESPONSABLE') or #id == principal.uid()")`
  sur les deux endpoints `/client/{id}` et `/client/{id}/search` : le personnel peut
  toujours consulter n'importe quel client, mais un `CLIENT` ne peut consulter que ses
  propres notifications (403 sinon). Un `@ExceptionHandler(AccessDeniedException.class)`
  dédié a été ajouté à `GlobalExceptionHandler` pour que ce refus remonte en 403 JSON
  cohérent plutôt qu'en 400 générique. Vérifié en conditions réelles : un client peut lire
  ses propres notifications (200) et se voit refuser celles d'un autre (403).
- **Bonus découvert en testant** : `GlobalExceptionHandler` importait par erreur
  `org.springframework.security.authentication.BadCredentialsException` (classe Spring
  Security) au lieu de `com.salma.mini_projet_pharmacie.exception.BadCredentialsException`
  (celle réellement levée par `AuthService`) — un mot de passe incorrect remontait donc en
  400 générique au lieu de 401. Corrigé (import supprimé, résolution vers la classe du
  même package).

**Ce qui reste un choix assumé, pas un oubli :**

- **Refresh token** : non implémenté. Le JWT expire après 1h (`security.jwt.expiration-ms`)
  et l'utilisateur doit se reconnecter — acceptable pour ce périmètre, mais une vraie
  UX de production voudrait un refresh token à rotation (cookie séparé).
- **Rate limiting** sur `/auth/login` : toujours absent (aucune protection anti
  brute-force). À ajouter (ex. bucket4j, ou au niveau reverse-proxy).
- **Coût BCrypt** : `BCryptPasswordEncoder()` par défaut (force 10). Un audit de charge
  pourrait ajuster ce facteur selon la capacité serveur réelle.

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

Résolu :
- Émission d'un vrai token de session, autorisation par rôle appliquée côté serveur,
  CORS restreint à une liste d'origines explicite avec `allowCredentials(true)`
  (`app.cors.allowed-origins`, voir `backend/.../WebConfig.java`) — remédiation JWT/RBAC
  (section 1).
- Mots de passe hachés BCrypt (inscription, création/modification pharmacien, connexion)
  + migration automatique des comptes existants ; IDOR sur `/notifications/client/{id}`
  corrigé par vérification d'appartenance (`uid` du JWT) — voir section 1bis.

Reste ouvert :

- Refresh token / durée de session glissante — actuellement expiration fixe 1h.
- Message métier dédié pour l'échec de suppression d'un fournisseur référencé (à
  l'image de ce qui existe déjà pour les produits dans `ProduitService`), pour éviter
  de dépendre d'une détection heuristique côté frontend.
- Rate limiting sur `/auth/login` (aucune protection contre le brute-force constatée).
- CSRF : `SameSite=Strict` + absence de formulaire HTML classique réduit déjà
  fortement le risque, mais si une requête `GET` produisait un jour un effet de bord,
  ajouter un token CSRF applicatif dédié.
