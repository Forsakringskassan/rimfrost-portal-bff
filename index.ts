import express from "express";
import { mockHandlaggare } from "./utils/mockDataService.js";
import { transformUppgift } from "./utils/transformUppgift.js";

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
 * GET /handlaggare
 * Fetch all available case handlers (with mock fallback)
 */
app.get("/handlaggare", async (req, res) => {
    const backendUrl = process.env.HANDLAGGARE_URL ?? "";

    try {
        const response = await fetch(backendUrl, { method: "GET" });
        if (!response.ok) throw new Error(`HTTP error: ${response.status}`);
        const data = await response.json();
        return res.json(data);
    } catch (error) {
        console.warn(`[FALLBACK] Backend unavailable, using mock handlaggare`);
        return res.json({ handlaggare: mockHandlaggare });
    }
});

/**
 * GET /tasks/:handlaggarId
 * Fetch all tasks assigned to a specific handler
 */
app.get("/tasks/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const oulUrl = process.env.BE_OUL_URL ?? "";
    const backendUrl = `${oulUrl}/uppgifter/handlaggare/${handlaggarId}`;

    try {
        const response = await fetch(backendUrl, { method: "GET" });

        if (!response.ok) {
            throw new Error(`HTTP error in tasks fetch. Status: ${response.status}`);
        }

        const data: any = await response.json();
        // Safety check to only fetch tasks that the handler are qualified for
        // return validateAndReturnData(data, handlaggarId);
        return res.json({
            ...data,
            operativa_uppgifter: data.operativa_uppgifter?.map((u: any) => transformUppgift(u)),
        });
    } catch (error) {
        console.error(`Error fetching tasks for handlaggarId ${handlaggarId}:`, error);
        return res.status(500).json({ error: `Error fetching tasks: ${error}` });
    }
});

/**
 * POST /tasks/getNext/:handlaggarId
 * Assign a new task to a handler
 */
app.post("/tasks/getNext/:handlaggarId", async (req, res) => {
    const { handlaggarId } = req.params;
    const oulUrl = process.env.BE_OUL_URL ?? "";
    const backendUrl = `${oulUrl}/uppgifter/handlaggare/${handlaggarId}`;

    try {
        const response = await fetch(backendUrl, {
            method: "POST",
            body: JSON.stringify(req.body),
            headers: { "Content-Type": "application/json" },
        });

        if (!response.ok) {
            console.error(`Error from backend when assigning task: ${response.status} - ${response.statusText}`);
            return res.status(502).json({ error: "Failed to assign task, backend error" });
        }

        const data = await response.json();
const operativUppgift = data.uppgift?.operativ_uppgift ?? data.operativ_uppgift;

if (!operativUppgift) {
    return res.status(404).json({ error: "No task in response from backend" });
}

return res.status(200).json({ uppgift: transformUppgift(operativUppgift) });
    } catch (error) {
        console.error(`Error assigning task to handlaggarId ${handlaggarId}:`, error);
        return res.status(500).json({ error: `Error assigning task: ${error}` });
    }
});

app.listen(port, () => {
    console.log(`BFF server running on http://localhost:${port}`);
});