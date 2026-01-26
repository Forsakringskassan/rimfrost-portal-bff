# Development Fallback System

This BFF includes a comprehensive fallback system for development that automatically provides mock data when the backend is unavailable.

## Overview

When working in development mode (`NODE_ENV !== 'production'`), if the backend at `http://localhost:8889` fails or is unavailable, the BFF will automatically serve mock data instead of returning an error.

## How It Works

### Architecture

1. **Mock Data Service** ([mockDataService.ts](mockDataService.ts))
   - Manages a pool of 20 pre-generated mock tasks
   - Tracks which tasks are assigned to which handlers
   - Can dynamically create new tasks when the pool is exhausted
   
2. **Proxy with Fallback** ([proxyWithFallback.ts](proxyWithFallback.ts))
   - Attempts to proxy requests to the real backend
   - Automatically falls back to mock data on failure (development only)
   - Logs all proxy attempts and fallbacks for visibility

### Mock Task Structure

Each mock task follows this format:
```json
{
  "uppgift_id": "001",
  "kundbehovsflode_id": "001",
  "skapad": "2026-01-15T08:00:00Z",
  "status": "Pågående",
  "handlaggar_id": "469ddd20-6796-4e05-9e18-6a95953f6cb3",
  "planerad_till": "2026-01-25T17:00:00Z",
  "utford": null,
  "kundbehov": "Vård av husdjur",
  "regel": "rtf-manuell",
  "beskrivning": "Manuell kontroll RTF",
  "verksamhetslogik": "Kontrollera om ansökande uppfyller kraven för vård av husdjur",
  "roll": "Handläggare",
  "url": "http://localhost:3031/vardAvHusdjur.js"
}
```

## API Endpoints

### Production Endpoints (with fallback)

#### GET /uppgifter/handlaggare/:handlaggarId
Fetches all tasks assigned to a specific handler.

**Request:**
```bash
GET http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
```

**Response:**
```json
{
  "operativa_uppgifter": [
    {
      "uppgift_id": "001",
      "kundbehovsflode_id": "001",
      ...
    }
  ]
}
```

**Behavior:**
- Attempts to fetch from `http://localhost:8889/uppgifter/handlaggare/:handlaggarId`
- On failure (development only): Returns mock tasks assigned to that handler
- On failure (production): Returns 502 error

#### POST /uppgifter/handlaggare/:handlaggarId
Assigns a new task to a handler.

**Request:**
```bash
POST http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
Content-Type: application/json

{}
```

**Response:**
```json
{
  "uppgift": {
    "uppgift_id": "001",
    "kundbehovsflode_id": "001",
    ...
  }
}
```

**Behavior:**
- Attempts to post to `http://localhost:8889/uppgifter/handlaggare/:handlaggarId`
- On failure (development only): Assigns a task from the mock pool and returns it
- On failure (production): Returns 502 error

### Development-Only Management Endpoints

These endpoints only work when `NODE_ENV !== 'production'` and allow you to manage mock data from external micro-frontends.

#### DELETE /uppgifter/handlaggare/:handlaggarId/uppgift/:uppgiftId
Removes a specific task from a handler's assigned list (simulates task completion/closure).

**Request:**
```bash
DELETE http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3/uppgift/001
```

**Response:**
```json
{
  "message": "Task removed successfully",
  "handlaggarId": "469ddd20-6796-4e05-9e18-6a95953f6cb3",
  "uppgiftId": "001",
  "remainingTasks": 2
}
```

#### GET /mock/tasks/stats
Get statistics about the current state of mock tasks.

**Request:**
```bash
GET http://localhost:9001/mock/tasks/stats
```

**Response:**
```json
{
  "totalTasks": 20,
  "assignedCount": 5,
  "unassignedCount": 15,
  "handlers": 2,
  "handlerDetails": [
    {
      "handlaggarId": "469ddd20-6796-4e05-9e18-6a95953f6cb3",
      "taskCount": 3
    }
  ]
}
```

#### GET /mock/tasks/all
Get all tasks in the pool (both assigned and unassigned).

**Request:**
```bash
GET http://localhost:9001/mock/tasks/all
```

**Response:**
```json
{
  "tasks": [
    {
      "uppgift_id": "001",
      "handlaggar_id": "469ddd20-6796-4e05-9e18-6a95953f6cb3",
      ...
    },
    {
      "uppgift_id": "002",
      "handlaggar_id": null,
      ...
    }
  ]
}
```

#### POST /mock/tasks/reset
Reset the mock data service to its initial state (20 unassigned tasks).

**Request:**
```bash
POST http://localhost:9001/mock/tasks/reset
```

**Response:**
```json
{
  "message": "Mock data service reset successfully",
  "stats": {
    "totalTasks": 20,
    "assignedCount": 0,
    "unassignedCount": 20,
    "handlers": 0,
    "handlerDetails": []
  }
}
```

## Usage Examples

### Basic Workflow

1. **Start the BFF** (backend can be down):
   ```bash
   npm run dev
   ```

2. **Assign tasks to a handler**:
   ```bash
   curl -X POST http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
   ```

3. **Fetch assigned tasks**:
   ```bash
   curl http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
   ```

4. **Complete a task (from external micro-frontend)**:
   ```bash
   curl -X DELETE http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3/uppgift/001
   ```

5. **Check stats**:
   ```bash
   curl http://localhost:9001/mock/tasks/stats
   ```

### From Your Frontend Application

```javascript
// Assign a new task
const assignTask = async () => {
  const response = await fetch(
    'http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3',
    { method: 'POST' }
  );
  const data = await response.json();
  console.log('Assigned task:', data.uppgift);
};

// Fetch all tasks
const fetchTasks = async () => {
  const response = await fetch(
    'http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3'
  );
  const data = await response.json();
  console.log('Tasks:', data.operativa_uppgifter);
};

// Remove/complete a task
const completeTask = async (taskId) => {
  const response = await fetch(
    `http://localhost:9001/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3/uppgift/${taskId}`,
    { method: 'DELETE' }
  );
  const data = await response.json();
  console.log('Task removed:', data);
};
```

## Console Output

The fallback system provides clear logging:

```
[PROXY] GET http://localhost:8889/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
[PROXY] Error: GET http://localhost:8889/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3 [Error details]
[FALLBACK] Using mock data for GET http://localhost:8889/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
```

When the backend is working:
```
[PROXY] GET http://localhost:8889/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
[PROXY] Success: GET http://localhost:8889/uppgifter/handlaggare/469ddd20-6796-4e05-9e18-6a95953f6cb3
```

## Configuration

The fallback system is controlled by the `NODE_ENV` environment variable:

- **Development** (`NODE_ENV !== 'production'` or not set): Fallback enabled
- **Production** (`NODE_ENV === 'production'`): Fallback disabled, returns 502 errors

The backend URL is currently hardcoded to `http://localhost:8889`. You can make this configurable by adding it to your `.env` file:

```env
BACKEND_URL=http://localhost:8889
```

## Benefits

1. **Never Blocked**: Continue development even when backend is down or has breaking changes
2. **Realistic Testing**: Mock data follows the exact structure of real backend responses
3. **Stateful Mocking**: Tasks can be assigned and removed, simulating real state changes
4. **External Control**: Other micro-frontends can manage mock tasks via DELETE endpoint
5. **Automatic**: No code changes needed - just works when backend fails
6. **Safe**: Only active in development, production gets proper error responses

## Notes

- The initial pool contains 20 tasks
- If all tasks are assigned, new ones are created automatically
- Task IDs (`uppgift_id`) and flow IDs (`kundbehovsflode_id`) vary per task
- `planerad_till` dates are randomized between 1-14 days from today
- All other fields remain static as specified
- The mock data persists in memory during the BFF's runtime but resets on server restart
