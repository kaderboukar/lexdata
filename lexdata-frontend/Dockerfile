# Étape 1 : Build de l'application React avec Node.js
FROM node:20-alpine AS builder

WORKDIR /app

# Installer les dépendances d'abord (cache layer)
COPY package.json package-lock.json* ./
RUN npm ci --include=dev


# Copier le code source et builder
COPY . .
# Même URL en prod : le navigateur appelle /api → nginx.conf proxy vers lexdata-gateway.
# (Les .env sont souvent exclus du contexte Docker ; sans ça, un vieux 8080 peut rester figé dans dist.)
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# Étape 2 : Serveur Nginx pour servir le build de production
FROM nginx:alpine AS production
RUN apk add --no-cache curl

# Copier le build de React dans le dossier Nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# Copier la configuration Nginx personnalisée
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
