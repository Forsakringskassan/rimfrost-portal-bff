/**
 * Example usage of the fallback API from your frontend application
 * 
 * This file demonstrates how to interact with the BFF endpoints
 * that have automatic fallback to mock data when the backend is down.
 */

const BFF_URL = 'http://localhost:9001';
const HANDLER_ID = '469ddd20-6796-4e05-9e18-6a95953f6cb3';

// ============================================================================
// Task Operations
// ============================================================================

/**
 * Fetch all tasks assigned to the handler
 */
export async function fetchAssignedTasks() {
  const response = await fetch(
    `${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`
  );
  
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks: ${response.statusText}`);
  }
  
  const data = await response.json();
  return data.operativa_uppgifter;
}

/**
 * Assign a new task to the handler
 */
export async function assignNewTask() {
  const response = await fetch(
    `${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({}) // Add any required payload here
    }
  );
  
  if (!response.ok) {
    throw new Error(`Failed to assign task: ${response.statusText}`);
  }
  
  const data = await response.json();
  return data.uppgift;
}

/**
 * Remove/complete a task (development only)
 * This endpoint only works when NODE_ENV !== 'production'
 */
export async function completeTask(uppgiftId) {
  const response = await fetch(
    `${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}/uppgift/${uppgiftId}`,
    {
      method: 'DELETE'
    }
  );
  
  if (!response.ok) {
    throw new Error(`Failed to complete task: ${response.statusText}`);
  }
  
  return await response.json();
}

// ============================================================================
// Mock Data Management (Development Only)
// ============================================================================

/**
 * Get statistics about mock tasks
 * Only works in development mode
 */
export async function getMockStats() {
  const response = await fetch(`${BFF_URL}/mock/tasks/stats`);
  
  if (!response.ok) {
    throw new Error(`Failed to get stats: ${response.statusText}`);
  }
  
  return await response.json();
}

/**
 * Get all mock tasks in the pool
 * Only works in development mode
 */
export async function getAllMockTasks() {
  const response = await fetch(`${BFF_URL}/mock/tasks/all`);
  
  if (!response.ok) {
    throw new Error(`Failed to get all tasks: ${response.statusText}`);
  }
  
  const data = await response.json();
  return data.tasks;
}

/**
 * Reset the mock data service to initial state
 * Only works in development mode
 */
export async function resetMockData() {
  const response = await fetch(
    `${BFF_URL}/mock/tasks/reset`,
    {
      method: 'POST'
    }
  );
  
  if (!response.ok) {
    throw new Error(`Failed to reset mock data: ${response.statusText}`);
  }
  
  return await response.json();
}

// ============================================================================
// Example Usage in a Vue 3 Component (Composition API)
// ============================================================================

/*
<script setup>
import { ref, onMounted } from 'vue';
import {
  fetchAssignedTasks,
  assignNewTask,
  completeTask,
  getMockStats
} from './api';

const tasks = ref([]);
const stats = ref(null);
const loading = ref(false);

// Load tasks on mount
onMounted(() => {
  loadTasks();
  loadStats();
});

const loadTasks = async () => {
  try {
    loading.value = true;
    const data = await fetchAssignedTasks();
    tasks.value = data;
  } catch (error) {
    console.error('Failed to load tasks:', error);
  } finally {
    loading.value = false;
  }
};

const loadStats = async () => {
  try {
    const data = await getMockStats();
    stats.value = data;
  } catch (error) {
    console.error('Failed to load stats:', error);
  }
};

const handleAssignTask = async () => {
  try {
    loading.value = true;
    const newTask = await assignNewTask();
    console.log('New task assigned:', newTask);
    await loadTasks();
    await loadStats();
  } catch (error) {
    console.error('Failed to assign task:', error);
  } finally {
    loading.value = false;
  }
};

const handleCompleteTask = async (taskId) => {
  try {
    loading.value = true;
    await completeTask(taskId);
    await loadTasks();
    await loadStats();
  } catch (error) {
    console.error('Failed to complete task:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div>
    <h1>Task Manager</h1>
    
    <div v-if="stats">
      <h2>Stats</h2>
      <p>Total Tasks: {{ stats.totalTasks }}</p>
      <p>Assigned: {{ stats.assignedCount }}</p>
      <p>Available: {{ stats.unassignedCount }}</p>
    </div>

    <button @click="handleAssignTask" :disabled="loading">
      Assign New Task
    </button>

    <h2>Assigned Tasks</h2>
    <p v-if="loading">Loading...</p>
    <ul v-else>
      <li v-for="task in tasks" :key="task.uppgift_id">
        <strong>{{ task.uppgift_id }}</strong> - {{ task.beskrivning }}
        <br />
        Planned: {{ new Date(task.planerad_till).toLocaleDateString() }}
        <br />
        <button @click="handleCompleteTask(task.uppgift_id)">
          Complete
        </button>
      </li>
    </ul>
  </div>
</template>
*/

// ============================================================================
// Example Usage in Vanilla JavaScript
// ============================================================================

/*
// Initialize
document.addEventListener('DOMContentLoaded', async () => {
  await loadTasks();
  
  document.getElementById('assignBtn').addEventListener('click', async () => {
    await assignNewTask();
    await loadTasks();
  });
});

async function loadTasks() {
  const tasks = await fetchAssignedTasks();
  const taskList = document.getElementById('taskList');
  
  taskList.innerHTML = tasks.map(task => `
    <div class="task">
      <h3>${task.uppgift_id} - ${task.beskrivning}</h3>
      <p>Planned: ${new Date(task.planerad_till).toLocaleDateString()}</p>
      <button onclick="handleComplete('${task.uppgift_id}')">Complete</button>
    </div>
  `).join('');
}

async function handleComplete(taskId) {
  await completeTask(taskId);
  await loadTasks();
}
*/
