export async function checkAllTasksQualification(handlaggarId: string) {
    const backendUrl = `${process.env.TASKS_URL}handlaggare/${handlaggarId}/check-all`;
    const response = await fetch(backendUrl, { method: 'POST'});

    if (!response.ok) {
        // Swap to error handling in prod
        // throw new Error(`HTTP error! status: ${response.status}`);
        return true;
    }

    return response.ok;
}

export async function checkSingleTaskQualification(handlaggarId: string, uppgiftId: string) {
    const backendUrl = `${process.env.TASKS_URL}handlaggare/${handlaggarId}/uppgift/${uppgiftId}`;
    const response = await fetch(backendUrl, { method: 'POST'});

    if (!response.ok) {
        // throw new Error(`HTTP error! status: ${response.status}`);
        return true;
    }

    return response.ok;
}