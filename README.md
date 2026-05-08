# Rimfrost Portal BFF

Backend for Frontend (BFF) for portal task handling.

The service exposes task-oriented endpoints used by the portal frontend and forwards requests to OUL backend services.

## Features

- Express API written in TypeScript
- CORS enabled (`*`) with support for preflight (`OPTIONS`)
- Health endpoint for runtime checks
- Task fetch and task assignment endpoints
- Response transformation from backend `operativa_uppgifter` to frontend-friendly shape
- **Dynamic remote registry** — serves the micro-frontend manifest at `GET /api/route-manifest`, read from a file that can be a Kubernetes ConfigMap mount (no rebuild needed to add or update a remote)

## Architecture

Communication flow:

```
[Host FE] <-> [Portal BFF] <-> [OUL Backend]
```

Current BFF responsibilities:

- Return tasks for a specific handlaggare
- Assign next task for a specific handlaggare
- Normalize task payload fields via `transformUppgift`
- Serve the micro-frontend remote registry (`remotes.json`) so the portal can discover and load remotes at runtime without any rebuild

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

| Variable | Required | Description |
|---|---|---|
| `BE_OUL_URL` | Yes | Base URL of the OUL backend. Used for task fetch and assignment calls. |
| `REMOTES_CONFIG_PATH` | No | Path to the micro-frontend registry JSON file. Defaults to `remotes.json` next to `index.ts`. Set this to a Kubernetes ConfigMap volume mount path in production. |

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

### `GET /api/route-manifest`

Returns the micro-frontend remote registry used by the portal to discover and load remotes at runtime.

The registry is read from disk on every request (no cache) so updates take effect immediately without a server restart.

**Registry source** (first match wins):
1. File at path specified by `REMOTES_CONFIG_PATH` env var (use for Kubernetes ConfigMap mount)
2. `remotes.json` in the same directory as `index.ts` (default, used in local dev)

**To add or update a remote** — edit `remotes.json` (dev) or update the ConfigMap (production). No rebuild of the portal or BFF is required.

Example response:

```json
{
  "routes": {
    "rtf-manuell": {
      "scope": "remoteApp",
      "module": "VardAvHusdjur",
      "devEntry": "http://localhost:3031/mf-manifest.json",
      "prodEntry": "https://cdn.example.com/rtf-manuell/mf-manifest.json"
    }
  }
}
```

Each entry:

| Field | Description |
|---|---|
| `scope` | Must match `federation({ name: ... })` in the remote's `vite.config.ts` |
| `module` | Exposed component key without the leading `./` |
| `devEntry` | URL to the remote's `mf-manifest.json` in development |
| `prodEntry` | URL to the remote's `mf-manifest.json` in production |

Error handling:

- `500` if the registry file is missing or cannot be parsed

## Project Structure

```
rimfrost-portal-bff/
|- index.ts
|- remotes.json          # Micro-frontend registry (dev default; replaced by ConfigMap in production)
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