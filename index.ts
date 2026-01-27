import express from "express";
import path from "path";
import { fileURLToPath } from "node:url";
import * as mockDataService from "./mockDataService.js";
import { proxyWithFallback } from "./proxyWithFallback.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = "9001"

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, OPTIONS");
    res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Content-Length, X-Requested-With");
    if (req.method === "OPTIONS") {
        res.sendStatus(200);
    } else {
        next();
    }
});

app.get("/api/health", (req, res) => {
    res.json({ status: "ok", timestamp: new Date().toISOString() });
});

app.get("/api/data", (req, res) => {
    res.json({ data: { message: "This is some example data from the BFF!" }});
})

app.get("/api/fetch-test-data", async (req, res) => {
    try {
        const response = await fetch('https://jsonplaceholder.typicode.com/todos/1');
        const data = await response.json();
        res.json(data);
    } catch (error) {
        console.error(`Error fetching test data: ${error}`);
        res.status(500).json({ error: "Failed to fetch test data" });
    }
})

// ============================================================================
// UPPGIFTER (TASKS) ENDPOINTS - With Development Fallback
// ============================================================================

/**
 * GET /uppgifter/handlaggare/:handlaggarId
 * Fetch all tasks assigned to a specific handler
 * Falls back to mock data in development when backend is unavailable
 */
app.get("/uppgifter/handlaggare/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const backendUrl = `http://localhost:8889/uppgifter/handlaggare/${handlaggarId}`;
    
    await proxyWithFallback(req, res, {
        targetUrl: backendUrl,
        method: 'GET',
        fallbackData: {
            operativa_uppgifter: mockDataService.getAssignedTasks(handlaggarId)
        },
        onSuccess: (data) => {
            // Trigger fallback if operativa_uppgifter is null
            if (data.operativa_uppgifter === null) {
                throw new Error('operativa_uppgifter returned null');
            }
            return data;
        }
    });
});

/**
 * POST /uppgifter/handlaggare/:handlaggarId
 * Assign a new task to a handler
 * Falls back to mock data in development when backend is unavailable
 */
app.post("/uppgifter/handlaggare/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const backendUrl = `http://localhost:8889/uppgifter/handlaggare/${handlaggarId}`;
    
    await proxyWithFallback(req, res, {
        targetUrl: backendUrl,
        method: 'POST',
        body: req.body,
        fallbackData: (() => {
            const newTask = mockDataService.assignTaskToHandler(handlaggarId);
            return {
                uppgift: newTask
            };
        })(),
        onSuccess: (data) => {
            // Trigger fallback if operativ_uppgift or uppgift is null
            if (data.operativ_uppgift === null || data.uppgift === null) {
                throw new Error('operativ_uppgift/uppgift returned null');
            }
            return data;
        }
    });
});

// ============================================================================
// MOCK DATA MANAGEMENT ENDPOINTS (Development Only)
// These endpoints allow you to manage the mock data from external microfront-ends
// ============================================================================

/**
 * DELETE /uppgifter/handlaggare/:handlaggarId/uppgift/:uppgiftId
 * Remove a specific task from a handler (simulates task closure/completion)
 * Only works in development
 */
app.delete("/uppgifter/handlaggare/:handlaggarId/uppgift/:uppgiftId", (req, res) => {
    if (process.env.NODE_ENV === 'production') {
        return res.status(404).json({ error: "Endpoint not available in production" });
    }

    const { handlaggarId, uppgiftId } = req.params;
    const success = mockDataService.removeTaskFromHandler(handlaggarId, uppgiftId);
    
    if (success) {
        res.json({
            message: "Task removed successfully",
            handlaggarId,
            uppgiftId,
            remainingTasks: mockDataService.getAssignedTasks(handlaggarId).length
        });
    } else {
        res.status(404).json({
            error: "Task not found or not assigned to this handler",
            handlaggarId,
            uppgiftId
        });
    }
});

/**
 * GET /mock/tasks/stats
 * Get statistics about mock tasks (for debugging)
 * Only works in development
 */
app.get("/mock/tasks/stats", (req, res) => {
    if (process.env.NODE_ENV === 'production') {
        return res.status(404).json({ error: "Endpoint not available in production" });
    }

    res.json(mockDataService.getStats());
});

/**
 * GET /mock/tasks/all
 * Get all tasks in the pool
 * Only works in development
 */
app.get("/mock/tasks/all", (req, res) => {
    if (process.env.NODE_ENV === 'production') {
        return res.status(404).json({ error: "Endpoint not available in production" });
    }

    res.json({
        tasks: mockDataService.getAllTasks()
    });
});

/**
 * POST /mock/tasks/reset
 * Reset the mock data service to initial state
 * Only works in development
 */
app.post("/mock/tasks/reset", (req, res) => {
    if (process.env.NODE_ENV === 'production') {
        return res.status(404).json({ error: "Endpoint not available in production" });
    }

    mockDataService.reset();
    res.json({
        message: "Mock data service reset successfully",
        stats: mockDataService.getStats()
    });
});

// ============================================================================
// STATIC FILES & FALLBACK
// ============================================================================

const pathToDistFE = path.join(__dirname, "../fe/dist");
app.use(express.static(pathToDistFE));

// app.get("/*path", (req, res) => {
//     if (!req.path.startsWith("/api/")) {
//         res.sendFile(path.join(pathToDistFE, "index.html"));
//     } else {
//         res.status(404).send("API endpoint not found");
//     }
// })

app.listen(port, async () => {
    console.log(`Example app listening on port ${port}`);
    console.log(`Serving static files from ${pathToDistFE}`);

    if (process.env.TEST_URL) {
        try {
            const response = await fetch(process.env.TEST_URL);
            const data = await response.json();
        } catch (error) {
            console.error('Error fetching from TEST_URL:', error);
        }
    }
})
