# ---- Frontend (Vue3 + Vite) build then serve with Nginx ----
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# Ensure .env.production is present and log it for debugging
RUN ls -la .env* || echo "No .env files found"
RUN cat .env.production || echo ".env.production not found"
# Use production build (package.json already sets NODE_OPTIONS)
RUN npm run build:prod
# Verify VUE_APP_BASE_API was properly embedded
RUN grep -r "VUE_APP_BASE_API" dist/ || echo "VUE_APP_BASE_API not found in build"

# Runtime stage
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html
# Clean default content
RUN rm -rf /usr/share/nginx/html/*
# Copy compiled assets
COPY --from=build /app/dist/ /usr/share/nginx/html/
# Custom nginx config (SPA and compression)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
