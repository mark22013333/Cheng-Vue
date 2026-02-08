# ---- Frontend (Vue3 + Vite) build then serve with Nginx ----
# 單一應用程式包含商城和後台管理

# Build stage
FROM node:18-alpine AS build

# 安裝 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# 複製 pnpm 相關檔案
COPY package.json pnpm-lock.yaml ./

# 使用 pnpm 安裝依賴
RUN pnpm install --frozen-lockfile --prod=false

COPY . .

# 建置（單一建置包含商城和後台）
RUN pnpm run build:prod

# 驗證建置結果
RUN ls -la dist/ && echo "✅ Build completed"

# Runtime stage
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# 清除預設內容
RUN rm -rf /usr/share/nginx/html/*

# 複製建置結果
COPY --from=build /app/dist/ /usr/share/nginx/html/

# Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
