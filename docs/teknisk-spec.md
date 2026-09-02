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
| POST | `/tasks/getNext` | Tilldela nästa tillgängliga uppgift |

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

## Liveness

`/q/health`, `/q/health/ready` (inkl. anrop mot OUL), `/q/health/live`.

## Kända begränsningar och framtida arbete

| Begränsning | Föreslagen åtgärd |
|---|---|
| Mockflaggans standardvärde skiljer sig mellan miljökonfiguration och kod | Enhetliggör standardvärdet för `portal.mock.handlaggare` |
| Felsvar saknar ett gemensamt, typat schema | Inför en enhetlig felresponsmodell |
| Ingen paginering på `/tasks` eller `/tasks/team` | Bedöm behov när uppgiftsvolymen växer |
