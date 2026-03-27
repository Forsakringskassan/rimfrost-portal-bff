// Mock data service for development fallback
// This service manages a pool of mock tasks and tracks which tasks are assigned to handlers

export interface MockTask {
    uppgift_id: string;
    handlaggning_id: string;
    skapad: string;
    status: string;
    handlaggar_id: string | null;
    planerad_till: string;
    utford: string | null;
    kundbehov: string;
    regel: string;
    beskrivning: string;
    verksamhetslogik: string;
    roll: string;
    url: string;
}

export interface MockHandlaggare {
    handlaggarId: string;
    fornamn: string;
    efternamn: string;
}

let taskPool: MockTask[] = [];
let assignedTasks: Map<string, MockTask[]> = new Map();
let nextTaskId: number = 1;

const FIXED_TASK_IDS = [
    { uppgift_id: 'task-001', handlaggning_id: 'Flow-001' },
    { uppgift_id: 'task-002', handlaggning_id: 'Flow-002' },
    { uppgift_id: 'task-003', handlaggning_id: 'Flow-003' },
    { uppgift_id: 'task-004', handlaggning_id: 'Flow-004' },
    { uppgift_id: 'task-005', handlaggning_id: 'Flow-005' },
    { uppgift_id: 'task-006', handlaggning_id: 'Flow-006' },
    { uppgift_id: 'task-007', handlaggning_id: 'Flow-007' },
    { uppgift_id: 'task-008', handlaggning_id: 'Flow-008' },
    { uppgift_id: 'task-009', handlaggning_id: 'Flow-009' },
    { uppgift_id: 'task-010', handlaggning_id: 'Flow-010' },
    { uppgift_id: 'task-011', handlaggning_id: 'Flow-011' },
    { uppgift_id: 'task-012', handlaggning_id: 'Flow-012' },
    { uppgift_id: 'task-013', handlaggning_id: 'Flow-013' },
    { uppgift_id: 'task-014', handlaggning_id: 'Flow-014' },
    { uppgift_id: 'task-015', handlaggning_id: 'Flow-015' },
    { uppgift_id: 'task-016', handlaggning_id: 'Flow-016' },
    { uppgift_id: 'task-017', handlaggning_id: 'Flow-017' },
    { uppgift_id: 'task-018', handlaggning_id: 'Flow-018' },
    { uppgift_id: 'task-019', handlaggning_id: 'Flow-019' },
    { uppgift_id: 'task-020', handlaggning_id: 'Flow-020' },
];

export const mockHandlaggare: MockHandlaggare[] = [
    {
        handlaggarId: '469ddd20-6796-4e05-9e18-6a95953f6cb3',
        fornamn: 'Lisa',
        efternamn: 'Tass',
    },
    {
        handlaggarId: '550e8400-e29b-41d4-a716-446655440001',
        fornamn: 'Karl',
        efternamn: 'von Vovve',
    },
    {
        handlaggarId: '550e8400-e29b-41d4-a716-446655440002',
        fornamn: 'Åsa',
        efternamn: 'Missesson',
    },
];

function initializeTaskPool(): void {
    const baseDate = new Date('2026-01-23');

    for (let i = 0; i < FIXED_TASK_IDS.length; i++) {
        const { uppgift_id, handlaggning_id } = FIXED_TASK_IDS[i];
        const planerad = new Date(baseDate);
        planerad.setDate(planerad.getDate() + Math.floor(Math.random() * 14) + 1);

        const handlaggarId = i < 5 ? (process.env.DEFAULT_HANDLER_ID ?? null) : null;

        const isBekreftaBeslut = i % 2 === 0;

        const task: MockTask = {
            uppgift_id,
            handlaggning_id,
            skapad: "2026-01-15T08:00:00Z",
            status: "Pågående",
            handlaggar_id: handlaggarId,
            planerad_till: planerad.toISOString(),
            utford: null,
            kundbehov: "Vård av husdjur",
            regel: isBekreftaBeslut ? "bekrafta-beslut" : "rtf-manuell",
            beskrivning: isBekreftaBeslut ? "Bekräfta beslut" : "Manuell kontroll RTF",
            verksamhetslogik: isBekreftaBeslut
                ? "Granska och bekräfta beslut för vård av husdjur"
                : "Kontrollera om ansökande uppfyller kraven för vård av husdjur",
            roll: "Handläggare",
            url: isBekreftaBeslut ? "bekrafta-beslut" : "rtf-manuell",
        };

        taskPool.push(task);

        if (handlaggarId) {
            if (!assignedTasks.has(handlaggarId)) {
                assignedTasks.set(handlaggarId, []);
            }
            assignedTasks.get(handlaggarId)!.push(task);
        }
    }

    nextTaskId = 21;
}

export function getAssignedTasks(handlaggarId: string): MockTask[] {
    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    return assignedTasks.get(handlaggarId)!;
}

export function assignTaskToHandler(handlaggarId: string): MockTask | null {
    const availableTask = taskPool.find(task => task.handlaggar_id === null);

    if (!availableTask) {
        return createAndAssignNewTask(handlaggarId);
    }

    availableTask.handlaggar_id = handlaggarId;

    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    assignedTasks.get(handlaggarId)!.push(availableTask);

    return availableTask;
}

function createAndAssignNewTask(handlaggarId: string): MockTask {
    const taskId = `task-${String(nextTaskId).padStart(3, '0')}`;
    const handlaggningId = `hdl-0000-0000-${String(nextTaskId).padStart(4, '0')}`;
    const planerad = new Date();
    planerad.setDate(planerad.getDate() + Math.floor(Math.random() * 14) + 1);

    const newTask: MockTask = {
        uppgift_id: taskId,
        handlaggning_id: handlaggningId,
        skapad: new Date().toISOString(),
        status: "Pågående",
        handlaggar_id: handlaggarId,
        planerad_till: planerad.toISOString(),
        utford: null,
        kundbehov: "Vård av husdjur",
        regel: "rtf-manuell",
        beskrivning: "Manuell kontroll RTF",
        verksamhetslogik: "Kontrollera om ansökande uppfyller kraven för vård av husdjur",
        roll: "Handläggare",
        url: "rtf-manuell",
    };

    nextTaskId++;
    taskPool.push(newTask);

    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    assignedTasks.get(handlaggarId)!.push(newTask);

    return newTask;
}

export function removeTaskFromHandler(handlaggarId: string, uppgiftId: string): boolean {
    if (!assignedTasks.has(handlaggarId)) {
        return false;
    }

    const tasks = assignedTasks.get(handlaggarId)!;
    const taskIndex = tasks.findIndex(task => task.uppgift_id === uppgiftId);

    if (taskIndex === -1) {
        return false;
    }

    tasks.splice(taskIndex, 1);

    const poolTask = taskPool.find(task => task.uppgift_id === uppgiftId);
    if (poolTask) {
        poolTask.handlaggar_id = null;
        poolTask.status = "Avslutad";
        poolTask.utford = new Date().toISOString();
    }

    return true;
}

export function getAllTasks(): MockTask[] {
    return taskPool;
}

export function reset(): void {
    taskPool = [];
    assignedTasks.clear();
    nextTaskId = 1;
    initializeTaskPool();
}

export function getStats() {
    const totalTasks = taskPool.length;
    const assignedCount = taskPool.filter(task => task.handlaggar_id !== null).length;
    const unassignedCount = totalTasks - assignedCount;

    return {
        totalTasks,
        assignedCount,
        unassignedCount,
        handlers: assignedTasks.size,
        handlerDetails: Array.from(assignedTasks.entries()).map(([id, tasks]) => ({
            handlaggarId: id,
            taskCount: tasks.length,
        })),
    };
}

initializeTaskPool();