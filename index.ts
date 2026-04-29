import express from "express";
import { mockHandlaggare } from "./utils/mockDataService.js";
import { transformUppgift } from "./utils/transformUppgift.js";
import validateAndReturnData from "./utils/validateAndReturnData.js";

const app = express();
const port = 9001;

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


async function fetchSkyddadIdentitet(individId: string): Promise<boolean> {
    const individUrl = process.env.BE_INDIVID_URL ?? "";
    try {
        const res = await fetch(`${individUrl}/individ/${individId}`);
        if (!res.ok) return false;
        const data = await res.json() as { skyddadIdentitet?: boolean };
        return data.skyddadIdentitet ?? false;
    } catch {
        return false;
    }
}

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
        return res.json({
            handlaggare: mockHandlaggare.map(h => ({
                ...h,
                harSIDBehorighet: h.behorigheter.includes('SID'),
            })),
        });
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
        const filteredTasks = await validateAndReturnData(data, handlaggarId);

        if (!filteredTasks) {
            return res.json({ operativa_uppgifter: [] });
        }

        const enrichedTasks = await Promise.all(
            filteredTasks.map(async (u: any) => ({
                ...u,
                skyddad_identitet: u.individer?.[0]
                    ? await fetchSkyddadIdentitet(u.individer[0])
                    : false,
            }))
        );
        return res.json({
            operativa_uppgifter: enrichedTasks.map((u: any) => transformUppgift(u)),
        });
    } catch (error) {
        console.warn(`[FALLBACK] Backend unavailable for handlaggarId ${handlaggarId}, returning empty task list`);
        return res.json({ operativa_uppgifter: [] });
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

        const data = await response.json() as { operativ_uppgift?: any };
        let uppgift = data.operativ_uppgift ?? null;
        if (uppgift?.individer?.[0]) {
            uppgift = { ...uppgift, skyddad_identitet: await fetchSkyddadIdentitet(uppgift.individer[0]) };
        }
        const transformed = uppgift ? transformUppgift(uppgift) : null;
        return res.status(200).json({ uppgift: transformed });
    } catch (error) {
        console.error(`Error assigning task to handlaggarId ${handlaggarId}:`, error);
        return res.status(500).json({ error: `Error assigning task: ${error}` });
    }
});

/**
 * POST /yrkande
 * Proxy for mina-sidor — creates a customer need
 */
// === DEMO ONLY — delete these two endpoints when mina-sidor demo is retired ===

/**
 * POST /yrkande
 * Proxy for mina-sidor — creates a customer need
 */
app.post("/yrkande", async (req, res) => {
    const backendUrl = `${process.env.BE_HANDLAGGNING_URL ?? ""}/yrkande`;
    try {
        const response = await fetch(backendUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json", "accept": "application/json" },
            body: JSON.stringify(req.body),
        });
        const text = await response.text();
        console.log(`[/yrkande] status=${response.status} body=${text}`);
        const data = text ? JSON.parse(text) : {};
        return res.status(response.status).json(data);
    } catch (error) {
        console.error(`[/yrkande] error:`, error);
        return res.status(502).json({ error: `Could not reach handlaggning backend: ${error}` });
    }
});

/**
 * POST /handlaggning
 * Proxy for mina-sidor — triggers the handlaggning flow
 */
app.post("/handlaggning", async (req, res) => {
    const backendUrl = `${process.env.BE_HANDLAGGNING_URL ?? ""}/handlaggning`;
    try {
        const response = await fetch(backendUrl, {
            method: "POST",
            headers: { "Content-Type": "application/json", "accept": "application/json" },
            body: JSON.stringify(req.body),
        });
        const text = await response.text();
        console.log(`[/handlaggning] status=${response.status} body=${text}`);
        const data = text ? JSON.parse(text) : {};
        return res.status(response.status).json(data);
    } catch (error) {
        console.error(`[/handlaggning] error:`, error);
        return res.status(502).json({ error: `Could not reach handlaggning backend: ${error}` });
    }
});

// === END DEMO ONLY ===

app.listen(port, () => {
    console.log(`BFF server running on http://localhost:${port}`);
});