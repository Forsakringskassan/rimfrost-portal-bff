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

class MockDataService {
    // Pool of all available mock tasks
    private taskPool: MockTask[] = [];
    // Map of handler ID to their assigned tasks
    private assignedTasks: Map<string, MockTask[]> = new Map();
    private nextTaskId: number = 1;

    constructor() {
        this.initializeTaskPool();
    }

    private initializeTaskPool(): void {
        // Create 20 mock tasks with varying IDs and dates
        const baseDate = new Date('2026-01-23');
        const defaultHandlerId = '469ddd20-6796-4e05-9e18-6a95953f6cb3';
        
        for (let i = 1; i <= 20; i++) {
            const taskId = String(i).padStart(3, '0');
            const kundbehovsflodeId = String(i).padStart(3, '0');
            const planerad = new Date(baseDate);
            planerad.setDate(planerad.getDate() + Math.floor(Math.random() * 14) + 1); // 1-14 days ahead
            
            // Pre-assign first 5 tasks to the default handler, rest remain unassigned
            const handlaggarId = i <= 5 ? defaultHandlerId : null;
            
            const task: MockTask = {
                uppgift_id: taskId,
                kundbehovsflode_id: kundbehovsflodeId,
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
                url: "http://localhost:3031/vardAvHusdjur.js"
            };
            
            this.taskPool.push(task);
            
            // Add pre-assigned tasks to the assignedTasks map
            if (handlaggarId) {
                if (!this.assignedTasks.has(handlaggarId)) {
                    this.assignedTasks.set(handlaggarId, []);
                }
                this.assignedTasks.get(handlaggarId)!.push(task);
            }
        }
        
        this.nextTaskId = 21; // Next available ID after the initial 20
    }

    /**
     * Get all tasks assigned to a specific handler
     */
    getAssignedTasks(handlaggarId: string): MockTask[] {
        if (!this.assignedTasks.has(handlaggarId)) {
            this.assignedTasks.set(handlaggarId, []);
        }
        return this.assignedTasks.get(handlaggarId)!;
    }

    /**
     * Assign a new task to a handler (simulates POST to assign task)
     * Returns the newly assigned task or null if no tasks available
     */
    assignTaskToHandler(handlaggarId: string): MockTask | null {
        // Find an unassigned task from the pool
        const availableTask = this.taskPool.find(task => task.handlaggar_id === null);
        
        if (!availableTask) {
            // If no tasks in pool, create a new one
            return this.createAndAssignNewTask(handlaggarId);
        }

        // Assign the task to the handler
        availableTask.handlaggar_id = handlaggarId;
        
        // Add to assigned tasks list
        if (!this.assignedTasks.has(handlaggarId)) {
            this.assignedTasks.set(handlaggarId, []);
        }
        this.assignedTasks.get(handlaggarId)!.push(availableTask);
        
        return availableTask;
    }

    /**
     * Create a new task and assign it to a handler
     */
    private createAndAssignNewTask(handlaggarId: string): MockTask {
        const taskId = String(this.nextTaskId).padStart(3, '0');
        const kundbehovsflodeId = String(this.nextTaskId).padStart(3, '0');
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
            url: "http://localhost:3031/vardAvHusdjur.js"
        };
        
        this.nextTaskId++;
        this.taskPool.push(newTask);
        
        if (!this.assignedTasks.has(handlaggarId)) {
            this.assignedTasks.set(handlaggarId, []);
        }
        this.assignedTasks.get(handlaggarId)!.push(newTask);
        
        return newTask;
    }

    /**
     * Remove a task from a handler's assigned tasks (simulates task completion/closure)
     */
    removeTaskFromHandler(handlaggarId: string, uppgiftId: string): boolean {
        if (!this.assignedTasks.has(handlaggarId)) {
            return false;
        }
        
        const tasks = this.assignedTasks.get(handlaggarId)!;
        const taskIndex = tasks.findIndex(task => task.uppgift_id === uppgiftId);
        
        if (taskIndex === -1) {
            return false;
        }
        
        // Remove from assigned tasks
        const [removedTask] = tasks.splice(taskIndex, 1);
        
        // Mark task as unassigned in the pool
        const poolTask = this.taskPool.find(task => task.uppgift_id === uppgiftId);
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
    getAllTasks(): MockTask[] {
        return this.taskPool;
    }

    /**
     * Reset the service to initial state
     */
    reset(): void {
        this.taskPool = [];
        this.assignedTasks.clear();
        this.nextTaskId = 1;
        this.initializeTaskPool();
    }

    /**
     * Get statistics about the current state
     */
    getStats() {
        const totalTasks = this.taskPool.length;
        const assignedCount = this.taskPool.filter(task => task.handlaggar_id !== null).length;
        const unassignedCount = totalTasks - assignedCount;
        
        return {
            totalTasks,
            assignedCount,
            unassignedCount,
            handlers: this.assignedTasks.size,
            handlerDetails: Array.from(this.assignedTasks.entries()).map(([id, tasks]) => ({
                handlaggarId: id,
                taskCount: tasks.length
            }))
        };
    }
}

// Export a singleton instance
export const mockDataService = new MockDataService();
