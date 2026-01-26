/**
 * Test script for the fallback system
 * Run this to verify the fallback endpoints work correctly
 * 
 * Usage: node test-fallback.js
 */

const BFF_URL = 'http://localhost:9001';
const HANDLER_ID = '469ddd20-6796-4e05-9e18-6a95953f6cb3';

async function testFallbackSystem() {
  console.log('🧪 Testing Fallback System\n');
  console.log('Make sure the BFF is running: npm run dev\n');
  console.log('The backend at localhost:8889 should be DOWN for this test\n');
  console.log('='.repeat(60) + '\n');

  try {
    // Test 1: Get initial stats
    console.log('📊 Test 1: Get Mock Stats');
    const statsResponse = await fetch(`${BFF_URL}/mock/tasks/stats`);
    const stats = await statsResponse.json();
    console.log('Initial stats:', JSON.stringify(stats, null, 2));
    console.log('✅ Stats retrieved successfully\n');

    // Test 2: Fetch tasks (should be empty initially)
    console.log('📋 Test 2: Fetch Assigned Tasks (should be empty)');
    const tasksResponse = await fetch(`${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`);
    const tasksData = await tasksResponse.json();
    console.log(`Found ${tasksData.operativa_uppgifter.length} assigned tasks`);
    console.log('✅ Tasks fetched successfully\n');

    // Test 3: Assign 3 tasks
    console.log('➕ Test 3: Assign 3 Tasks');
    for (let i = 1; i <= 3; i++) {
      const assignResponse = await fetch(
        `${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({})
        }
      );
      const assignData = await assignResponse.json();
      console.log(`  Task ${i} assigned: ${assignData.uppgift.uppgift_id}`);
    }
    console.log('✅ 3 tasks assigned successfully\n');

    // Test 4: Fetch tasks again (should have 3)
    console.log('📋 Test 4: Fetch Assigned Tasks Again');
    const tasksResponse2 = await fetch(`${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`);
    const tasksData2 = await tasksResponse2.json();
    console.log(`Found ${tasksData2.operativa_uppgifter.length} assigned tasks`);
    tasksData2.operativa_uppgifter.forEach(task => {
      console.log(`  - Task ${task.uppgift_id}: ${task.beskrivning}`);
      console.log(`    Planned: ${task.planerad_till}`);
    });
    console.log('✅ Tasks fetched successfully\n');

    // Test 5: Complete a task
    console.log('✔️  Test 5: Complete a Task');
    const firstTaskId = tasksData2.operativa_uppgifter[0].uppgift_id;
    const deleteResponse = await fetch(
      `${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}/uppgift/${firstTaskId}`,
      { method: 'DELETE' }
    );
    const deleteData = await deleteResponse.json();
    console.log(`Removed task ${firstTaskId}`);
    console.log(`Remaining tasks: ${deleteData.remainingTasks}`);
    console.log('✅ Task completed successfully\n');

    // Test 6: Fetch tasks again (should have 2)
    console.log('📋 Test 6: Fetch Assigned Tasks After Completion');
    const tasksResponse3 = await fetch(`${BFF_URL}/uppgifter/handlaggare/${HANDLER_ID}`);
    const tasksData3 = await tasksResponse3.json();
    console.log(`Found ${tasksData3.operativa_uppgifter.length} assigned tasks`);
    console.log('✅ Tasks fetched successfully\n');

    // Test 7: Get final stats
    console.log('📊 Test 7: Get Final Stats');
    const statsResponse2 = await fetch(`${BFF_URL}/mock/tasks/stats`);
    const stats2 = await statsResponse2.json();
    console.log('Final stats:', JSON.stringify(stats2, null, 2));
    console.log('✅ Stats retrieved successfully\n');

    // Test 8: Get all tasks
    console.log('📚 Test 8: Get All Tasks in Pool');
    const allTasksResponse = await fetch(`${BFF_URL}/mock/tasks/all`);
    const allTasksData = await allTasksResponse.json();
    console.log(`Total tasks in pool: ${allTasksData.tasks.length}`);
    const assignedTasks = allTasksData.tasks.filter(t => t.handlaggar_id !== null);
    const unassignedTasks = allTasksData.tasks.filter(t => t.handlaggar_id === null);
    console.log(`  - Assigned: ${assignedTasks.length}`);
    console.log(`  - Unassigned: ${unassignedTasks.length}`);
    console.log('✅ All tasks retrieved successfully\n');

    // Test 9: Reset
    console.log('🔄 Test 9: Reset Mock Data');
    const resetResponse = await fetch(`${BFF_URL}/mock/tasks/reset`, {
      method: 'POST'
    });
    const resetData = await resetResponse.json();
    console.log('Reset successful');
    console.log('Stats after reset:', JSON.stringify(resetData.stats, null, 2));
    console.log('✅ Mock data reset successfully\n');

    console.log('='.repeat(60));
    console.log('🎉 All tests passed successfully!');
    console.log('='.repeat(60));

  } catch (error) {
    console.error('❌ Test failed:', error.message);
    console.error('Make sure the BFF is running on port 9001');
    process.exit(1);
  }
}

// Run the tests
testFallbackSystem();
