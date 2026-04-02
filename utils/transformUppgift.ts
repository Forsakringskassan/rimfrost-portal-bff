type RawOperativUppgift = {
  uppgift_id: string;
  handlaggning_id: string;
  skapad: string;
  status: string;
  handlaggar_id: string;
  planerad_till?: string;
  utford?: string;
  kundbehov: string;
  regel: string;
  beskrivning: string;
  verksamhetslogik: string;
  roll: string;
  url: string;
};

export function transformUppgift(rawOperativUppgift: RawOperativUppgift) {
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