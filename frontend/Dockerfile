# ----- Build Stage -----
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files and install dependencies (cached layer)
COPY package.json package-lock.json* ./
RUN npm ci --silent

# Copy source code and build
COPY . .
RUN npm run build

# ----- Runtime Stage -----
FROM nginx:1.25-alpine

# Copy custom nginx config as a template for envsubst
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Default backend URL for local compose
ENV BACKEND_URL="http://backend:8080"

# Copy built frontend
COPY --from=build /app/dist /usr/share/nginx/html

# Security: remove default nginx pages
RUN rm -rf /usr/share/nginx/html/50x.html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
