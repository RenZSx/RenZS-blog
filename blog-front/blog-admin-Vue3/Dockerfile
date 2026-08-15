FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build:prod

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
RUN mkdir -p /usr/share/nginx/html/upload
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
