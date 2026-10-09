# 阶段一：构建前端静态页面
FROM node:20-alpine AS builder
WORKDIR /app

# 设置 npm 镜像源加速构建
RUN npm config set registry https://registry.npmmirror.com

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# 阶段二：生产级轻量 Nginx 镜像
FROM nginx:alpine
LABEL maintainer="honghe-data-party"

# 复制自定义 Nginx 配置与构建产物
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
