# Rimfrost Portal BFF

Backend for Frontend (BFF) for portal task handling.

The service exposes task-oriented endpoints used by the portal frontend and forwards requests to OUL backend services.

## Features

- Express API written in TypeScript
- CORS enabled (`*`) with support for preflight (`OPTIONS`)
- Health endpoint for runtime checks
- Task fetch and task assignment endpoints
- Response transformation from backend `operativa_uppgifter` to frontend-friendly shape

## Architecture

Communication flow:

```
[Host FE] <-> [Portal BFF] <-> [OUL Backend]
```

Current BFF responsibilities:

- Return tasks for a specific handlaggare
- Assign next task for a specific handlaggare
- Normalize task payload fields via `transformUppgift`

## Prerequisites

- Node.js 20+
- npm

## Quick Start

```bash
npm install
npm run dev
```

The service starts on `http://localhost:9001`.

## Scripts

- `npm run dev` - Run with hot reload (`tsx --watch`) and `.env` loading
- `npm run build` - Compile TypeScript to `dist/`
- `npm run start` - Run compiled output (`dist/index.js`) with `.env`
- `npm run type-check` - TypeScript check without emitting files
- `npm run lint` - Lint source
- `npm run lint:fix` - Auto-fix lint issues
- `npm run format` - Format code with Prettier
- `npm run format:check` - Verify formatting

## Environment Variables

Create `.env` in the project root:

```env
NODE_ENV=development
BE_OUL_URL=http://localhost:8889
```

Notes:

- `BE_OUL_URL` is used for both task fetch and task assignment backend calls.
- If `BE_OUL_URL` is missing, backend requests will fail due to invalid target URL.

## API

### `GET /api/health`

Returns service status.

Example response:

```json
{
    "status": "ok",
    "timestamp": "2026-03-30T12:34:56.789Z"
}
```

### `GET /tasks/:handlaggarId`

Fetches tasks from:

`{BE_OUL_URL}/uppgifter/handlaggare/:handlaggarId`

Returns backend payload with `operativa_uppgifter` transformed through `utils/transformUppgift.ts`.

Error handling:

- `500` if backend call fails or returns non-OK status

### `POST /tasks/getNext/:handlaggarId`

Assigns next task via:

`POST {BE_OUL_URL}/uppgifter/handlaggare/:handlaggarId`

Forwards request body as JSON.

Error handling:

- `502` when backend responds with non-OK status
- `500` on request/transport failures

## Project Structure

```
rimfrost-portal-bff/
|- index.ts
|- utils/
|  |- transformUppgift.ts
|  |- checkTaskQualification.ts
|  |- compareHandlerQualifications.ts
|  |- fetchHandlerQualifications.ts
|  |- validateAndReturnData.ts
|- package.json
|- tsconfig.json
```

## Implementation Notes

- The active routes are implemented in `index.ts`.
- The task qualification and fallback helper utilities exist in `utils/`, but are not currently wired into the active routes.