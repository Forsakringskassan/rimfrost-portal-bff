export function transformUppgift(rawOperativUppgift: any) {
  return {
    uppgiftId: rawOperativUppgift.uppgift_id,
    handlaggningId: rawOperativUppgift.handlaggning_id,
    skapad: rawOperativUppgift.skapad,
    status: rawOperativUppgift.status,
    handlaggarId: rawOperativUppgift.handlaggar_id,
    planeradTill: rawOperativUppgift.planerad_till || "",
    utford: rawOperativUppgift.utford || "",
    kundbehov: rawOperativUppgift.kundbehov,
    regel: rawOperativUppgift.regel,
    beskrivning: rawOperativUppgift.beskrivning,
    verksamhetslogik: rawOperativUppgift.verksamhetslogik,
    roll: rawOperativUppgift.roll,
    url: rawOperativUppgift.url,
  };
}