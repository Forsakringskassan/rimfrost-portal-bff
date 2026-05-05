export default function compareHandlerQualifications(handlerQualifications: string[], taskQualifications: string[]): boolean {
    return taskQualifications.every(qualification => handlerQualifications.includes(qualification));
}