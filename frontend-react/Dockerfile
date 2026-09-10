# ---- Etape 1 : build ----
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# IMPORTANT : Vite integre les variables VITE_* dans le bundle JS au moment du
# build (import.meta.env est resolu statiquement), pas au demarrage du
# container. Pour changer l'URL de l'API, il faut reconstruire l'image avec ce
# build-arg, pas juste changer une variable d'environnement au "docker run".
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

# Horodatage reel du build (UTC), injecte dans index.html a la place de
# __BUILD_TIME__ : permet de confirmer via view-source/DevTools qu'un
# navigateur charge bien ce build precis et non une version en cache.
RUN BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ") && \
    sed -i "s/__BUILD_TIME__/$BUILD_TIME/" /app/dist/index.html

# ---- Etape 2 : service statique ----
FROM nginx:alpine
# L'image nginx:alpine publiee embarque parfois un util-linux/libuuid plus
# ancien que le correctif Alpine deja disponible (vu via scan Trivy : CVE
# HIGH sur libuuid, "Fixed Version" annoncee mais pas encore dans l'image
# de base). On force la mise a jour des paquets systeme au lieu d'ignorer
# ces CVE, pour rester patché meme si l'image officielle tarde a etre
# republiee.
RUN apk update && apk upgrade --no-cache

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
