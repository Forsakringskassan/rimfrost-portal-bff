export default async function fetchHandlerQualifications(handlaggarId: string) {
    const backendUrl = `http://future-url/${handlaggarId}`;
    const response = await fetch(backendUrl, { method: 'GET'});

    if (!response.ok) {
        // Swap to error handling in prod
        // throw new Error(`HTTP error! status: ${response.status}`);
        return [ 'behorighet1', 'behorighet2' ]; // Return mock qualifications on error
    }

    const data: any = await response.json();
    console.log('Fake data: ' + JSON.stringify(data.behorigheter));

    return [ 'behorighet1', 'behorighet2' ]; // Return mock qualifications
}