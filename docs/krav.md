# Krav — Portal BFF (PBFF)

## Bakgrund och syfte

Portal BFF är backend-för-frontend för handläggarportalen. Den ger portalens värdapplikation ett enda, stabilt gränssnitt mot Operativt Uppgiftslager (OUL) och döljer OUL:s interna datamodell och namngivningskonvention (snake_case) bakom ett frontend-vänligt (camelCase) kontrakt. BFF:n äger även den module federation-registrering som styr vilka mikrofrontends (regel-mikrofrontends) portalen kan ladda in, samt tillhandahåller tillfällig mockdata för handläggarinformation i väntan på en riktig källa.

---

## Intressenter och aktörer

| Aktör | Roll |
|---|---|
| Handläggarportalen (värdapplikation) | Anropar BFF:n för att lista, tilldela och hämta uppgifter samt för att läsa module federation-registret |
| Operativt Uppgiftslager (OUL) | Uppströmstjänst som äger uppgiftsdata och tilldelning av handläggare |
| Handläggare | Slutanvändare i portalen vars uppgiftslista och tilldelningar denna BFF förmedlar |

---

## Funktionella krav

### PBFF-FR-01 — Module federation-register

- **PBFF-FR-01.1** BFF:n ska exponera en ändpunkt som returnerar registret över tillgängliga
  regel-mikrofrontends (scope, modul och url:er) till portalens värdapplikation.
- **PBFF-FR-01.2** Registret ska kunna hämtas från en extern, monterad konfigurationsfil om en
  sådan är angiven, annars från en medföljande standardkonfiguration.
- **PBFF-FR-01.3** Om registerkonfigurationen saknas eller inte kan läsas ska anropet resultera
  i ett tydligt felsvar.

### PBFF-FR-02 — Handläggarinformation

- **PBFF-FR-02.1** BFF:n ska kunna tillhandahålla en lista över handläggare via en dedikerad
  ändpunkt, styrd av en konfigurerbar flagga, i väntan på en riktig källa för handläggardata.
- **PBFF-FR-02.2** Om handläggardata inte är tillgänglig ska anropet resultera i ett tydligt
  felsvar snarare än en tom eller felaktig lista.

### PBFF-FR-03 — Hämta och tilldela uppgifter

- **PBFF-FR-03.1** BFF:n ska kunna hämta samtliga uppgifter tilldelade den anropande
  handläggaren från OUL, baserat på handläggarens identitet i anropets auktoriseringsuppgifter.
- **PBFF-FR-03.2** BFF:n ska kunna hämta samtliga uppgifter tilldelade den anropande
  handläggarens team från OUL.
- **PBFF-FR-03.3** BFF:n ska kunna tilldela en specifik, namngiven uppgift till den anropande
  handläggaren.
- **PBFF-FR-03.4** BFF:n ska kunna tilldela nästa tillgängliga uppgift till den anropande
  handläggaren.
- **PBFF-FR-03.5** Uppgiftsdata från OUL ska normaliseras till ett konsekvent, frontend-vänligt
  format innan det returneras, inklusive att alltid returnera tomma listor istället för utelämnat
  värde när OUL inte har några uppgifter att lämna.

### PBFF-FR-04 — Felhantering vid integration mot OUL

- **PBFF-FR-04.1** Om OUL svarar med ett felstatus ska BFF:n vidarebefordra samma HTTP-statuskod
  till anropande klient tillsammans med diagnostisk information om felet.
- **PBFF-FR-04.2** Om OUL inte går att nå (nätverksfel) ska BFF:n särskilja detta från ett
  OUL-svarat fel och returnera en statuskod som tydligt anger att uppströmstjänsten är otillgänglig.
- **PBFF-FR-04.3** Oväntade fel som inte kan hänföras till OUL-integrationen ska resultera i ett
  generellt felsvar utan att exponera intern feldetalj till klienten.

---

## Icke-funktionella krav

### PBFF-NFR-01 — Observerbarhet

- **PBFF-NFR-01.1** BFF:n ska exponera hälsokontroller som speglar dess egen status samt
  tillgängligheten hos OUL.
- **PBFF-NFR-01.2** Relevanta identifierare (t.ex. handläggar- och uppgifts-id) ska ingå i
  loggkontext för att möjliggöra spårning av enskilda anrop genom systemet.

### PBFF-NFR-02 — Säkerhet

- **PBFF-NFR-02.1** BFF:n ska vidarebefordra anropande handläggares auktoriseringsuppgifter till
  OUL oförändrade, utan att själv tolka eller lagra dem.

---

## API-gränssnitt (översikt)

| API | Målgrupp | Specifikationsartefakt |
|---|---|---|
| Portal BFF REST-API | Handläggarportalen (värdapplikation) | Definieras i denna tjänst (ingen extern OpenAPI-specifikation ännu) |

---

## Integration med OUL

Portal BFF är en ren synkron REST-till-REST-integration mot OUL. Den lagrar inget tillstånd
själv — all uppgifts- och tilldelningsdata ägs och persisteras av OUL. BFF:ns ansvar är
begränsat till formatöversättning, felhanteringskonsekvens och att förmedla anropande
handläggares identitet vidare.
