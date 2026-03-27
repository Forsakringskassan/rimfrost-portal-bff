# Rimfrost FE BFF (Backend for Frontend)

This is a BFF service that provides an API layer between the frontend and backend services.

## Features

- **Express-based API** serving frontend requests
- **Development Fallback System**: Automatically serves mock data when the backend is unavailable (development only)
- **CORS enabled** for cross-origin requests
- **Static file serving** for frontend assets
- **TypeScript** for type safety

## System Architecture

### Communication Flow

This is the **Portal BFF** in a micro-frontend architecture:

```
[Host FE] ←→ [Portal BFF (This)] ←→ [Backend Services (port 8889)]
    ↓
[Micro FE] ←→ [Rule BFF] ←→ [Backend Services (port 8890)]
```

**Responsibilities:**

- Serves task list data to host frontend
- Handles task assignment and management
- Provides mock data management endpoints for development
- Acts as the primary data source for operational tasks (uppgifter)

### Unified Fallback System

**Centralized Data Resilience:**
All backend communication includes automatic fallback to mock data. This ensures seamless development regardless of backend availability.

**Environment-Driven Behavior:**

```env
BACKEND_BASE_URL=http://localhost:8889
FALLBACK_MODE=auto    # auto | always | never
FALLBACK_TIMEOUT_MS=5000
HANDLAGGARE_URL=http://localhost:8888/handlaggare
```

**Fallback Modes:**

- `auto`: Try backend first, fallback on failure (development default)
- `always`: Always use mock data (offline development)
- `never`: Fail fast, no fallback (production)

**Key Features:**

- ✅ Stateful mock task management with realistic data
- ✅ Consistent with Rule BFF fallback patterns
- ✅ Zero fallback logic in frontends
- ✅ Production-safe (fallback disabled)

📖 **See [FALLBACK.md](FALLBACK.md) for complete documentation**

## Quick Start

```bash
# Install dependencies
npm install

# Run in development mode (with hot reload)
npm run dev

# Build for production
npm run build

# Run production build
npm start
```

## Environment Variables

Create a `.env` file in the root directory:

```env
NODE_ENV=development
```

## API Endpoints

### Health Check

- `GET /api/health` - Returns server status

### Task Management (with fallback)

- `GET /uppgifter/handlaggare/:handlaggarId` - Fetch tasks for a handler
- `POST /uppgifter/handlaggare/:handlaggarId` - Assign a new task to a handler

### Development Only

- `DELETE /uppgifter/handlaggare/:handlaggarId/uppgift/:uppgiftId` - Remove a task
- `GET /mock/tasks/stats` - Get mock data statistics
- `GET /mock/tasks/all` - Get all mock tasks
- `POST /mock/tasks/reset` - Reset mock data

## Testing the Fallback System

```bash
# Make sure backend at localhost:8889 is DOWN
# Start the BFF
npm run dev

# In another terminal, run the test script
node test-fallback.js
```

## Project Structure

```
rimfrost-fe-bff/
├── index.ts                    # Main server file
├── mockDataService.ts          # Mock data management
├── proxyWithFallback.ts        # Proxy with fallback logic
├── test-fallback.js           # Test script
├── assets/
│   └── fallback.json          # Fallback data reference
├── examples/
│   └── frontend-usage.js      # Frontend integration examples
└── FALLBACK.md                # Fallback system documentation
```
