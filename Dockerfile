# Stage 1: Compile TypeScript
FROM node:22-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build && \
    node -e "const fs=require('fs'); const p=JSON.parse(fs.readFileSync('package.json','utf8')); p.imports={'#*':'./dist/*'}; fs.writeFileSync('package.json',JSON.stringify(p,null,2))"

# Stage 2: Production image
FROM node:22-alpine
WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=builder /app/dist ./dist
COPY --from=builder /app/package.json ./package.json

ENV NODE_ENV=production

EXPOSE 9001

CMD ["node", "dist/index.js"]
