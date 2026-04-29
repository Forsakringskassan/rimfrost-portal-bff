import { mockHandlaggare } from './mockDataService.js';

export default async function fetchHandlerQualifications(handlaggarId: string) {
    const backendUrl = `http://future-url/${handlaggarId}`;
    try {
        const response = await fetch(backendUrl, { method: 'GET'});

        if (!response.ok) {
            const handler = mockHandlaggare.find(h => h.handlaggarId === handlaggarId);
            return handler?.behorigheter ?? [];
        }

        const data: any = await response.json();
        return data.behorigheter ?? [];
    } catch {
        const handler = mockHandlaggare.find(h => h.handlaggarId === handlaggarId);
        return handler?.behorigheter ?? [];
    }
}
