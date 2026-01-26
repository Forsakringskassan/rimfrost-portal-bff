# Rimfrost FE BFF (Backend for Frontend)

This is a BFF service that provides an API layer between the frontend and backend services.

## Features

- **Express-based API** serving frontend requests
- **Development Fallback System**: Automatically serves mock data when the backend is unavailable (development only)
- **CORS enabled** for cross-origin requests
- **Static file serving** for frontend assets
- **TypeScript** for type safety

## Development Fallback System

This BFF includes an automatic fallback system that serves mock data when the backend at `localhost:8889` is unavailable or returns errors. This ensures you can continue development even when the backend is down or experiencing breaking changes.

**Key features:**
- ✅ Automatically falls back to mock data when backend fails
- ✅ Stateful mock tasks that can be assigned and removed
- ✅ Management endpoints for external micro-frontends
- ✅ Only active in development mode (safe for production)
- ✅ Realistic task data matching backend schema

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
