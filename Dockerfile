# Stage 1: install production dependencies only
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN mkdir -p /app/node_modules && npm install --omit=dev --no-audit --no-fund

# Stage 2: minimal runtime, non-root user
FROM node:20-alpine
ENV NODE_ENV=production PORT=3000
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY package.json ./
COPY src ./src
COPY public ./public
USER node
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://127.0.0.1:3000/health || exit 1
CMD ["node", "src/server.js"]
