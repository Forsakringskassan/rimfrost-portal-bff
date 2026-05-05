import { checkAllTasksQualification } from "./checkTaskQualification.js";
import compareHandlerQualifications from "./compareHandlerQualifications.js";
import fetchHandlerQualifications from "./fetchHandlerQualifications.js";

export default async function validateAndReturnData(data: any, handlaggarId: string) {
    const validHandlerQualifications = await checkAllTasksQualification(handlaggarId);
    if (!validHandlerQualifications) {
        return null;
    };
    
    const handlerQualifications = await fetchHandlerQualifications(handlaggarId) || [];
    const qualifiedTasks = data.operativa_uppgifter.filter((task: any) => {
        const taskQualifications = task.behorigheter || [];
        return compareHandlerQualifications(handlerQualifications, taskQualifications);
    })

    return qualifiedTasks;
}