export interface HandlaggarId {
    typId: string;
    varde: string;
}

export interface MockHandlaggare {
    handlaggarId: HandlaggarId;
    fornamn: string;
    efternamn: string;
}

export const mockHandlaggare: MockHandlaggare[] = [
    {
        handlaggarId: { typId: '116759e4-18fd-4209-849c-90abbd257d22', varde: '469ddd20-6796-4e05-9e18-6a95953f6cb3' },
        fornamn: 'Lisa',
        efternamn: 'Tass',
    },
    {
        handlaggarId: { typId: '550e8400-e29b-41d4-a716-446655440001', varde: '19850601-5678' },
        fornamn: 'Karl',
        efternamn: 'von Dobberman',
    },
    {
        handlaggarId: { typId: '550e8400-e29b-41d4-a716-446655440002', varde: '19721115-9012' },
        fornamn: 'Åsa',
        efternamn: 'Ormsäter',
    },
];