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

# ---- Etape 2 : service statique ----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
