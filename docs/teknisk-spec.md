# Teknisk spec — Portal BFF (PBFF)

## Översikt

Synkron REST BFF utan egen datalagring och utan meddelandeintegration. Uppströmsberoenden är
OUL och SID-tjänsten, båda nådda via REST-klientbibliotek. Ingen Kafka, ingen databas.

## Komponentstruktur

```text
src/main/java/se/fk/github/portalbff
├── PortalBffController      # REST-ändpunkter mot portalens värdapplikation
├── OulClient                # REST-klient mot OUL
├── SidClient                # REST-klient mot SID-tjänsten
├── UppgiftMapper            # Snake_case (OUL) -> camelCase (frontend) + null-normalisering
├── OulHealthCheck            # Egen readiness-koll mot OUL
├── SidHealthCheck            # Egen readiness-koll mot SID-tjänsten
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
| POST | `/sid/status` | Kontroll av skyddad identitet för angivna individer |

## Kafka-integration

Ingen. Tjänsten har ingen meddelandeintegration.

## Konfiguration

| Egenskap | Beskrivning | Standardvärde |
|---|---|---|
| `quarkus.rest-client.oul.url` (`BE_OUL_URL`) | Bas-URL till OUL | `http://localhost:8889` |
| `quarkus.rest-client.sid.url` (`BE_SID_URL`) | Bas-URL till SID-tjänsten | `http://localhost:8892` |
| `portal.remotes.config.path` (`PORTAL_REMOTES_CONFIG_PATH`) | Extern override för modulfederationsregistret | Medföljande standardregister |
| `portal.mock.handlaggare` (`PORTAL_MOCK_HANDLAGGARE`) | Aktiverar mockad handläggarlista | `false` |

## Liveness

`/q/health`, `/q/health/ready` (inkl. anrop mot OUL och SID-tjänsten), `/q/health/live`.

## Kända begränsningar och framtida arbete

| Begränsning | Föreslagen åtgärd |
|---|---|
| `typId`/`varde` i förfrågningskroppen till `/tasks` och `/tasks/getNext` skickas inte längre vidare till OUL sedan auktoriseringsuppgifter infördes | Ta bort fälten från kontraktet eller dokumentera dem som enbart avsedda för loggning |
| Mockflaggans standardvärde skiljer sig mellan miljökonfiguration och kod | Enhetliggör standardvärdet för `portal.mock.handlaggare` |
| Felsvar saknar ett gemensamt, typat schema | Inför en enhetlig felresponsmodell |
| Ingen paginering på `/tasks` eller `/tasks/team` | Bedöm behov när uppgiftsvolymen växer |
| `/sid/status` vidarebefordrar inte anropande handläggares auktoriseringsuppgifter, till skillnad från OUL-integrationen | Bekräfta om SID-tjänsten ska kräva auktorisering framöver |
