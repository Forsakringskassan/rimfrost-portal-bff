export interface MockHandlaggare {
    handlaggarId: string;
    fornamn: string;
    efternamn: string;
    behorigheter: string[];
}

export const mockHandlaggare: MockHandlaggare[] = [
    {
        handlaggarId: '469ddd20-6796-4e05-9e18-6a95953f6cb3',
        fornamn: 'Lisa',
        efternamn: 'Tass',
        behorigheter: [],
    },
    {
        handlaggarId: '550e8400-e29b-41d4-a716-446655440001',
        fornamn: 'Karl',
        efternamn: 'von Dobberman',
        behorigheter: ['SID'],
    },
    {
        handlaggarId: '550e8400-e29b-41d4-a716-446655440002',
        fornamn: 'Åsa',
        efternamn: 'Ormsäter',
        behorigheter: [],
    },
];
