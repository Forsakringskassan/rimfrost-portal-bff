# Rimfrost Portal BFF (Backend for Frontend)

This is a BFF service that provides an API layer between the portal frontend and backend services.

## Features

- **Express-based API** serving frontend requests
- **Development Fallback System**: Automatically serves mock data when the backend is unavailable
- **CORS enabled** for cross-origin requests
- **TypeScript** for type safety

## System Architecture

### Communication Flow

This is the **Portal BFF** in a micro-frontend architecture:
```
[Host FE] ←→ [Portal BFF (This)] ←→ [Backend Services]
    ↓
[Micro FE] ←→ [Rule BFF] ←→ [Backend Services]
```

**Responsibilities:**

- Serves task list data to host frontend
- Handles task assignment and management
- Provides handläggare list for dropdown selection
- Acts as the primary data source for operational tasks (uppgifter)

### Fallback System

**Centralized Data Resilience:**
All backend communication includes automatic fallback to mock data. This ensures seamless development regardless of backend availability.

**Environment-Driven Behavior:**

```env
BACKEND_BASE_URL=http://localhost:8889
FALLBACK_MODE=auto    # auto | always | never
FALLBACK_TIMEOUT_MS=5000
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
DEFAULT_HANDLER_ID=469ddd20-6796-4e05-9e18-6a95953f6cb3
TASKS_URL=http://localhost:8889/uppgifter/
HANDLAGGARE_URL=http://localhost:8888/handlaggare
```

## API Endpoints

### Health Check

- `GET /api/health` - Returns server status

### Handläggare

- `GET /handlaggare` - Fetch list of available case handlers (with mock fallback)

### Task Management

- `GET /tasks/:handlaggarId` - Fetch all tasks assigned to a specific handler
- `POST /tasks/getNext/:handlaggarId` - Assign a new task to a handler

## Project Structure
```
rimfrost-portal-bff/
├── index.ts                              # Main server file
└── utils/
    ├── mockDataService.ts                # Mock data for tasks and handläggare
    ├── transformUppgift.ts               # Data transformation
    ├── proxyWithFallback.ts              # Proxy with fallback logic
    ├── checkTaskQualification.ts         # Task qualification check
    ├── compareHandlerQualifications.ts   # Handler qualification comparison
    ├── fetchHandlerQualifications.ts     # Fetch handler qualifications
    └── validateAndReturnData.ts          # Data validation
```