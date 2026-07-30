FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci --silent

COPY . .

ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN npm run build

FROM nginxinc/nginx-unprivileged:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 8080