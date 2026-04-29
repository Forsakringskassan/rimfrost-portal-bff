export async function checkAllTasksQualification(handlaggarId: string) {
    const backendUrl = `${process.env.TASKS_URL}handlaggare/${handlaggarId}/check-all`;
    try {
        const response = await fetch(backendUrl, { method: 'POST'});
        if (!response.ok) {
            return true;
        }
        return response.ok;
    } catch {
        return true;
    }
}

export async function checkSingleTaskQualification(handlaggarId: string, uppgiftId: string) {
    const backendUrl = `${process.env.TASKS_URL}handlaggare/${handlaggarId}/uppgift/${uppgiftId}`;
    try {
        const response = await fetch(backendUrl, { method: 'POST'});
        if (!response.ok) {
            return true;
        }
        return response.ok;
    } catch {
        return true;
    }
}