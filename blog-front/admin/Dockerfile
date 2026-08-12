FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
ENV NODE_OPTIONS=--openssl-legacy-provider
RUN npm ci --legacy-peer-deps --registry=https://registry.npmmirror.com
COPY . .
RUN npx --no-install vue-cli-service build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
RUN mkdir -p /usr/share/nginx/html/upload
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
