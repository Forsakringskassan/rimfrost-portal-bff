# Teknisk spec — Portal BFF (PBFF)

## Översikt

Synkron REST BFF utan egen datalagring och utan meddelandeintegration. Enda uppströmsberoende
är OUL, nått via ett REST-klientbibliotek. Ingen Kafka, ingen databas.

## Komponentstruktur

```text
src/main/java/se/fk/github/portalbff
├── PortalBffController      # REST-ändpunkter mot portalens värdapplikation
├── OulClient                # REST-klient mot OUL
├── UppgiftMapper            # Snake_case (OUL) -> camelCase (frontend) + null-normalisering
├── OulHealthCheck            # Egen readiness-koll mot OUL
└── model/                   # DTO:er (Raw*-varianter = OUL:s form, övriga = frontend-form)
```

## API-specifikationer

Ingen extern OpenAPI-specifikation finns ännu för detta API — kontraktet definieras av
kontrollerklassen i denna tjänst.

| Metod | Sökväg | Beskrivning |
|---|---|---|
| GET | `/api/route-manifest` | Modulfederationsregister |
| GET | `/handlaggare` | Mockad handläggarlista (flaggstyrd) |
| POST | `/tasks` | Uppgifter tilldelade anropande handläggare |
| GET | `/tasks/team` | Uppgifter tilldelade anropande handläggares team |
| POST | `/tasks/{uppgiftId}/reassign` | Tilldela angiven uppgift till anropande handläggare |
| POST | `/tasks/{uppgiftId}/unassign` | Låt anropande handläggare lämna tillbaka angiven uppgift |
| POST | `/tasks/getNext` | Tilldela nästa tillgängliga uppgift |
| POST | `/tasks/search` | Sök uppgifter med status Ny för ett personnummer, bland uppgifter som inte delas ut via kön |

### Sök uppgifter för individ (`/tasks/search`)

Body: `{ "personnummer": "ÅÅÅÅMMDD-NNNN" }` (bindestreck valfritt). Personnumret ligger i bodyn
för att inte hamna i URL:er och åtkomstloggar, och loggas inte av BFF:n — inte heller OUL:s
felbody, som kan innehålla det. BFF:n normaliserar till `ÅÅÅÅMMDD-NNNN` och anropar OUL
`GET /uppgifter/individ/{id_typ}/{id_varde}?assignable=false`, där `id_typ` kommer från
`portal.oul.personnummer-typ-id`. Annat format ger 400 utan anrop till OUL.

`id_typ` är ett FK-internt referensdata-id som inte nödvändigtvis är känt i förväg, och fler
identitetstyper kan tillkomma. Därför är värdet konfigurerbart och inte hårdkodat.

Svaret har samma form som `/tasks`, filtrerat till uppgifter med status `Ny`.
`borttagna_pga_behorighet` är alltid 0, eftersom OUL:s individsökning saknar fältet;
SID-filtreringen sker i OUL. Fel från OUL: 400 och 403 vidarebefordras, 5xx vidarebefordras,
övriga statuskoder (t.ex. 404) blir 502.

### Vidarebefordran av behörighetssignaler

OUL:s svar för `/tasks` och `/tasks/team` innehåller fältet `borttagna_pga_behorighet` — antal
uppgifter som togs bort ur listan för att de blivit SID-märkta och handläggaren saknar
SID-behörighet. BFF:n vidarebefordrar fältet oförändrat i sitt eget svar utan egen tolkning;
ingen SID- eller behörighetslogik finns i denna tjänst.

### `typId`/`varde` i förfrågningskroppen (`/tasks`, `/tasks/getNext`)

Sedan auktoriseringsuppgifter infördes avgörs vems data OUL returnerar uteslutande av
`Authorization`-headern — `typId`/`varde` i förfrågningskroppen skickas inte längre vidare till
OUL. `typId` finns kvar, men enbart som klient-uppgiven kontext för loggkorrelation; den är
inte verifierad och loggas uttryckligen som sådan (`clientTypId=... (unverified)`), aldrig som
anropande handläggares faktiska identitet. `varde` är helt borttaget ur kontraktet, eftersom
det inte lästes någonstans (Jackson ignorerar tyst fältet om en klient ändå skickar det).

## Kafka-integration

Ingen. Tjänsten har ingen meddelandeintegration.

## Konfiguration

| Egenskap | Beskrivning | Standardvärde |
|---|---|---|
| `quarkus.rest-client.oul.url` (`BE_OUL_URL`) | Bas-URL till OUL | `http://localhost:8889` |
| `portal.remotes.config.path` (`PORTAL_REMOTES_CONFIG_PATH`) | Extern override för modulfederationsregistret | Medföljande standardregister |
| `portal.mock.handlaggare` (`PORTAL_MOCK_HANDLAGGARE`) | Aktiverar mockad handläggarlista | `false` |
| `portal.oul.personnummer-typ-id` (`PORTAL_PERSONNUMMER_TYP_ID`) | `id_typ` för personnummer i OUL:s individsökning | `c5f2e2b4-9143-4160-8f4b-30c172f0ac05` |

## Liveness

`/q/health`, `/q/health/ready` (inkl. anrop mot OUL), `/q/health/live`.

## Kända begränsningar och framtida arbete

| Begränsning | Föreslagen åtgärd |
|---|---|
| Mockflaggans standardvärde skiljer sig mellan miljökonfiguration och kod | Enhetliggör standardvärdet för `portal.mock.handlaggare` |
| Felsvar saknar ett gemensamt, typat schema | Inför en enhetlig felresponsmodell |
| Ingen paginering på `/tasks`, `/tasks/team` eller `/tasks/search` | Bedöm behov när uppgiftsvolymen växer |
| Personnumret ligger i URL:en i anropet till OUL:s individsökning (`GET`) och kan synas i OUL:s åtkomstloggar | OUL byter till `POST` med personnumret i bodyn (FKPOC-1123); anpassa `OulClient.searchIndividTasks` när kontraktet finns i oul-openapi |
