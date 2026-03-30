// Mock data service for development fallback
// This service manages a pool of mock tasks and tracks which tasks are assigned to handlers

export interface MockTask {
    uppgift_id: string;
    kundbehovsflode_id: string;
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

// Fixed IDs for consistent mapping across micro-frontends fallback data
const FIXED_TASK_IDS = [
    { uppgift_id: 'task-001', kundbehovsflode_id: 'flow-001' },
    { uppgift_id: 'task-002', kundbehovsflode_id: 'flow-002' },
    { uppgift_id: 'task-003', kundbehovsflode_id: 'flow-003' },
    { uppgift_id: 'task-004', kundbehovsflode_id: 'flow-004' },
    { uppgift_id: 'task-005', kundbehovsflode_id: 'flow-005' },
    { uppgift_id: 'task-006', kundbehovsflode_id: 'flow-006' },
    { uppgift_id: 'task-007', kundbehovsflode_id: 'flow-007' },
    { uppgift_id: 'task-008', kundbehovsflode_id: 'flow-008' },
    { uppgift_id: 'task-009', kundbehovsflode_id: 'flow-009' },
    { uppgift_id: 'task-010', kundbehovsflode_id: 'flow-010' },
    { uppgift_id: 'task-011', kundbehovsflode_id: 'flow-011' },
    { uppgift_id: 'task-012', kundbehovsflode_id: 'flow-012' },
    { uppgift_id: 'task-013', kundbehovsflode_id: 'flow-013' },
    { uppgift_id: 'task-014', kundbehovsflode_id: 'flow-014' },
    { uppgift_id: 'task-015', kundbehovsflode_id: 'flow-015' },
    { uppgift_id: 'task-016', kundbehovsflode_id: 'flow-016' },
    { uppgift_id: 'task-017', kundbehovsflode_id: 'flow-017' },
    { uppgift_id: 'task-018', kundbehovsflode_id: 'flow-018' },
    { uppgift_id: 'task-019', kundbehovsflode_id: 'flow-019' },
    { uppgift_id: 'task-020', kundbehovsflode_id: 'flow-020' },
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
    // Create 20 mock tasks with fixed IDs for consistent mapping
    const baseDate = new Date('2026-01-23');
    
    for (let i = 0; i < FIXED_TASK_IDS.length; i++) {
        const { uppgift_id, kundbehovsflode_id } = FIXED_TASK_IDS[i];
        const planerad = new Date(baseDate);
        planerad.setDate(planerad.getDate() + Math.floor(Math.random() * 14) + 1);
        
        // Pre-assign first 5 tasks to the default handler
        const handlaggarId = i < 5 ? (process.env.DEFAULT_HANDLER_ID ?? null) : null;
        
        const task: MockTask = {
            uppgift_id,
            kundbehovsflode_id,
            skapad: "2026-01-15T08:00:00Z",
            status: "Pågående",
            handlaggar_id: handlaggarId,
            planerad_till: planerad.toISOString(),
            utford: null,
            kundbehov: "Vård av husdjur",
            regel: "rtf-manuell",
            beskrivning: "Manuell kontroll RTF",
            verksamhetslogik: "Kontrollera om ansökande uppfyller kraven för vård av husdjur",
            roll: "Handläggare",
            url: "/regel/rtf-manuell"
        };
        
        taskPool.push(task);
        
        // Add pre-assigned tasks to the assignedTasks map
        if (handlaggarId) {
            if (!assignedTasks.has(handlaggarId)) {
                assignedTasks.set(handlaggarId, []);
            }
            assignedTasks.get(handlaggarId)!.push(task);
        }
    }
    
    nextTaskId = 21; // Next available ID after the initial 20
}

/**
 * Get all tasks assigned to a specific handler
 */
export function getAssignedTasks(handlaggarId: string): MockTask[] {
    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    return assignedTasks.get(handlaggarId)!;
}

/**
 * Assign a new task to a handler (simulates POST to assign task)
 * Returns the newly assigned task or creates and assigns a new one if none available
 */
export function assignTaskToHandler(handlaggarId: string): MockTask | null {
    const availableTask = taskPool.find(task => task.handlaggar_id === null);
    
    if (!availableTask) {
        return createAndAssignNewTask(handlaggarId);
    }

    // Assign the task to the handler
    availableTask.handlaggar_id = handlaggarId;
    
    // Add to assigned tasks list
    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    assignedTasks.get(handlaggarId)!.push(availableTask);
    
    return availableTask;
}

/**
 * Create a new task and assign it to a handler
 */
function createAndAssignNewTask(handlaggarId: string): MockTask {
    const taskId = `task-${String(nextTaskId).padStart(3, '0')}`;
    const kundbehovsflodeId = `flow-${String(nextTaskId).padStart(3, '0')}`;
    const planerad = new Date();
    planerad.setDate(planerad.getDate() + Math.floor(Math.random() * 14) + 1);
    
    const newTask: MockTask = {
        uppgift_id: taskId,
        kundbehovsflode_id: kundbehovsflodeId,
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
        url: "/regel/rtf-manuell"
    };
    
    nextTaskId++;
    taskPool.push(newTask);
    
    if (!assignedTasks.has(handlaggarId)) {
        assignedTasks.set(handlaggarId, []);
    }
    assignedTasks.get(handlaggarId)!.push(newTask);
    
    return newTask;
}

/**
 * Remove a task from a handler's assigned tasks (simulates task completion/closure)
 */
export function removeTaskFromHandler(handlaggarId: string, uppgiftId: string): boolean {
    if (!assignedTasks.has(handlaggarId)) {
        return false;
    }
    
    const tasks = assignedTasks.get(handlaggarId)!;
    const taskIndex = tasks.findIndex(task => task.uppgift_id === uppgiftId);
    
    if (taskIndex === -1) {
        return false;
    }
    
    // Remove from assigned tasks
    const [removedTask] = tasks.splice(taskIndex, 1);
    
    // Mark task as unassigned in the pool
    const poolTask = taskPool.find(task => task.uppgift_id === uppgiftId);
    if (poolTask) {
        poolTask.handlaggar_id = null;
        poolTask.status = "Avslutad";
        poolTask.utford = new Date().toISOString();
    }
    
    return true;
}

/**
 * Get all tasks in the pool (for debugging/management)
 */
export function getAllTasks(): MockTask[] {
    return taskPool;
}

/**
 * Reset the service to initial state
 */
export function reset(): void {
    taskPool = [];
    assignedTasks.clear();
    nextTaskId = 1;
    initializeTaskPool();
}

/**
 * Get statistics about the current state
 */
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
            taskCount: tasks.length
        }))
    };
}

// Initialize on module load
initializeTaskPool();
