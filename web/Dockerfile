# --- Build stage ---
FROM node:20-alpine AS builder
WORKDIR /app

# Install deps
COPY package.json package-lock.json* pnpm-lock.yaml* yarn.lock* ./
RUN if [ -f pnpm-lock.yaml ]; then npm i -g pnpm && pnpm i; \
    elif [ -f yarn.lock ]; then yarn install; \
    else npm install; fi

# Copy source & build
COPY . .
RUN npm run build

# --- Runtime stage ---
FROM node:20-alpine AS runner
WORKDIR /app

# Copy only what we need to run
COPY --from=builder /app/build ./build
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/package-lock.json* ./ 
COPY --from=builder /app/pnpm-lock.yaml* ./
COPY --from=builder /app/yarn.lock* ./

# Install production deps
RUN if [ -f pnpm-lock.yaml ]; then npm i -g pnpm && pnpm i --prod --frozen-lockfile; \
    elif [ -f yarn.lock ]; then yarn install --production --frozen-lockfile; \
    else npm install --omit=dev; fi

# Expose SvelteKit (adapter-node) default port
EXPOSE 3000

# Start the SvelteKit server
CMD ["node", "build"]