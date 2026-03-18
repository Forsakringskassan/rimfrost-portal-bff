import express from "express";
import path from "path";
import { fileURLToPath } from "node:url";
import * as mockDataService from "./utils/mockDataService.js";
import { transformUppgift } from "./utils/transformUppgift.js";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const port = "9001";

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

/**
 * GET /tasks/:handlaggarId
 * Fetch all tasks assigned to a specific handler
 */
app.get("/tasks/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const backendUrl = `${process.env.TASKS_URL ?? ""}handlaggare/${handlaggarId}`;

    try {
        const response = await fetch(backendUrl, { method: 'GET' });

        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }

        const data: any = await response.json();
        const uppgifter = data.operativa_uppgifter;

        if (!uppgifter || uppgifter.length === 0) {
            throw new Error('No tasks from backend, using fallback');
        }

        return res.json({ operativa_uppgifter: uppgifter.map((u: any) => transformUppgift(u)) });

    } catch (error) {
        console.warn(`[FALLBACK] Backend unavailable or empty, using mock data`);
        const tasks = mockDataService.getAssignedTasks(handlaggarId);
        return res.json({ operativa_uppgifter: tasks.map(transformUppgift) });
    }
});

/**
 * POST /tasks/getNext/:handlaggarId
 * Assign a new task to a handler
 */
app.post("/tasks/getNext/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const backendUrl = `${process.env.TASKS_URL ?? ""}handlaggare/${handlaggarId}`;

    try {
        const response = await fetch(backendUrl, { method: 'POST', body: JSON.stringify(req.body), headers: { 'Content-Type': 'application/json' } });

        if (!response.ok) {
            throw new Error(`HTTP error: ${response.status}`);
        }

        const data: any = await response.json();
        const uppgift = data.operativ_uppgift;

        if (!uppgift) {
            throw new Error('No task assigned from backend, using fallback');
        }

        return res.status(200).json({ uppgift: transformUppgift(uppgift) });

    } catch (error) {
        console.warn(`[FALLBACK] Backend unavailable or empty, using mock data`);
        const task = mockDataService.assignTaskToHandler(handlaggarId);
        if (!task) {
            return res.status(404).json({ error: 'No tasks available' });
        }
        return res.status(200).json({ uppgift: transformUppgift(task) });
    }
});

app.listen(port, () => {
    console.log(`BFF server running on http://localhost:${port}`);
});