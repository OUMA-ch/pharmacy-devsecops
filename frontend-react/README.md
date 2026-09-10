# PharmaHOSS — Frontend React

Frontend web React remplaçant l'application desktop JavaFX PharmaHOSS, consommant le
backend Spring Boot existant (`../backend`) sans aucune modification de celui-ci.

## Stack

React 18 + TypeScript (`strict: true`) + Vite + Tailwind CSS + React Router 6 +
TanStack Query + React Hook Form/Zod + Recharts. Tests avec Vitest + React Testing
Library. Lint/format avec ESLint (flat config) + Prettier.

## Installation

```bash
npm install
cp .env.example .env   # ajuster VITE_API_BASE_URL si besoin
```

## Lancement en développement

```bash
npm run dev
```

Ouvre l'app sur http://localhost:5173, avec l'API pointée par `VITE_API_BASE_URL`
(par défaut `http://localhost:8080`, le backend Spring Boot en local).

## Build de production

```bash
npm run build     # tsc -b && vite build -> dist/
npm run preview   # sert dist/ localement pour verification
```

**Important** : Vite intègre les variables `VITE_*` dans le bundle JS **au moment du
build**, pas au démarrage. Changer l'URL de l'API en production nécessite de
reconstruire le build (ou l'image Docker) avec la bonne valeur, pas juste de changer
une variable d'environnement au runtime.

## Tests

```bash
npm test          # une seule passe (CI)
npm run test:watch
```

Suite couvrant les formulaires critiques : connexion (`LoginPage`), ajout produit
(`ProduitFormModal`), nouvelle vente avec/sans ordonnance (`VenteFormModal`), nouvelle
commande avec lignes et validation d'erreur (`CommandeFormModal`).

## Lint / format

```bash
npm run lint
npm run format         # applique
npm run format:check   # verifie sans modifier
```

## Docker

```bash
docker build -t pharmahoss-frontend --build-arg VITE_API_BASE_URL=https://mon-api.example.com .
docker run -p 8080:80 pharmahoss-frontend
```

Image multi-stage : build Node 20, puis service statique via nginx (avec fallback SPA
sur `index.html` pour les routes React Router, voir `nginx.conf`).

## Endpoints backend utilisés

Voir le détail complet (endpoints confirmés vs hypothèses) dans la section
correspondante du message de livraison. Base : aucun endpoint inventé, tout vérifié
directement dans `backend/src/main/java/.../controller/*.java`.

## Authentification

Le backend émet un JWT signé à la connexion (`POST /auth/login`), transporté dans un
cookie `HttpOnly`/`SameSite=Strict` — jamais lu ni manipulé par le JS du frontend
(`credentials: "include"` sur chaque appel API pour qu'il parte automatiquement). Le
RBAC est appliqué **côté serveur** (`SecurityConfig.java`) sur chaque endpoint ; les
gardes de route React (`ProtectedRoute`) restent utiles pour l'UX (éviter un flash de
contenu interdit) mais ne sont plus le seul rempart. Détail complet et points encore
ouverts (IDOR résiduel, mots de passe en clair, pas de refresh token) dans
`README-SECURITY.md`.

## Limites connues (voir aussi README-SECURITY.md)

- **Notifications "lu/non lu"** : aucune route backend ne permet de marquer une
  notification comme lue. Le clic sur une carte la marque visuellement comme lue en
  mémoire locale (non persisté, revient à l'état serveur au rechargement).
- **Suppression fournisseur** : aucune garde métier côté backend (contrairement aux
  produits) ; une contrainte SQL peut remonter un message technique, intercepté et
  remplacé par un message générique côté frontend.
- **Statut de commande** : `String` libre côté backend (pas d'enum) ; le frontend
  restreint l'UI à 4 valeurs par convention (`EN_ATTENTE`, `VALIDEE`, `LIVREE`,
  `ANNULEE`).
