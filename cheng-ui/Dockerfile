# ---- Frontend (Vue2) build then serve with Nginx ----
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --no-audit --no-fund
COPY . .
# Use production build (package.json already sets NODE_OPTIONS)
RUN npm run build:prod

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
