# Stage 1: Compile TypeScript
FROM node:22-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Stage 2: Production image
FROM node:22-alpine
WORKDIR /app

# Pass version info and image name as build args
ARG NEXT_VERSION
ARG GIT_COMMIT
ARG IMAGE_NAME
ARG BUILD_DATE
ENV NEXT_VERSION=$NEXT_VERSION
ENV GIT_COMMIT=$GIT_COMMIT
ENV IMAGE_NAME=$IMAGE_NAME
ENV BUILD_DATE=$BUILD_DATE

ENV NODE_ENV=production
ENV PORT=9001

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=builder /app/dist ./dist

# Run as non-root user for security (matches OpenShift/CRC constraints)
RUN chown -R node:node .
USER node

EXPOSE 9001

CMD ["node", "dist/index.js"]
