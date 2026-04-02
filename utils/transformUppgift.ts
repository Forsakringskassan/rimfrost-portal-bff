interface RawUppgift {
  uppgift_id?: string;
  handlaggning_id?: string;
  skapad?: string;
  status?: string;
  handlaggar_id?: string | null;
  planerad_till?: string | null;
  utford?: string | null;
  kundbehov?: string;
  regel?: string;
  beskrivning?: string;
  verksamhetslogik?: string;
  roll?: string;
  url?: string;
}

export function transformUppgift(rawOperativUppgift: RawUppgift) {
  if (!rawOperativUppgift || typeof rawOperativUppgift !== "object") {
    throw new Error("transformUppgift: ogiltigt indata");
  }

  return {
    uppgiftId: rawOperativUppgift.uppgift_id ?? "",
    handlaggningId: rawOperativUppgift.handlaggning_id ?? "",
    skapad: rawOperativUppgift.skapad ?? "",
    status: rawOperativUppgift.status ?? "",
    handlaggarId: rawOperativUppgift.handlaggar_id ?? null,
    planeradTill: rawOperativUppgift.planerad_till ?? "",
    utford: rawOperativUppgift.utford ?? "",
    kundbehov: rawOperativUppgift.kundbehov ?? rawOperativUppgift.yrkande ?? "",
    regel: rawOperativUppgift.regel ?? "",
    beskrivning: rawOperativUppgift.beskrivning ?? "",
    verksamhetslogik: rawOperativUppgift.verksamhetslogik ?? "",
    roll: rawOperativUppgift.roll ?? "",
    url: rawOperativUppgift.url ?? "",
  };
}
