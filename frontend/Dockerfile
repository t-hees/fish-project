# Build Stage
FROM node:25-alpine AS build

ARG VITE_BACKEND_PORT
ENV VITE_BACKEND_PORT=${VITE_BACKEND_PORT}
ARG VITE_BACKEND_HOST
ENV VITE_BACKEND_HOST=${VITE_BACKEND_HOST}

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production Stage
FROM docker.io/library/nginx:stable-alpine AS production
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]