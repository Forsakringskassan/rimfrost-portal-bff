package se.fk.github.portalbff;

import org.junit.jupiter.api.Test;
import se.fk.github.portalbff.model.HandlaggarId;
import se.fk.github.portalbff.model.OperativUppgift;
import se.fk.github.portalbff.model.RawOperativUppgift;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UppgiftMapperTest {

    private static RawOperativUppgift fullRaw() {
        RawOperativUppgift raw = new RawOperativUppgift();
        raw.uppgiftId = "id-1";
        raw.handlaggningId = "hid-1";
        raw.skapad = "2024-01-01";
        raw.status = "AKTIV";
        raw.handlaggarId = new HandlaggarId();
        raw.planeradTill = "2024-02-01";
        raw.utford = "2024-02-02";
        raw.individer = List.of();
        raw.regel = "regel-1";
        raw.beskrivning = "beskrivning";
        raw.verksamhetslogik = "vl-1";
        raw.roll = "roll-1";
        raw.url = "http://example.com";
        return raw;
    }

    @Test
    void transform_mapsAllFields() {
        RawOperativUppgift raw = fullRaw();

        OperativUppgift result = UppgiftMapper.transform(raw);

        assertEquals(raw.uppgiftId, result.uppgiftId);
        assertEquals(raw.handlaggningId, result.handlaggningId);
        assertEquals(raw.skapad, result.skapad);
        assertEquals(raw.status, result.status);
        assertSame(raw.handlaggarId, result.handlaggarId);
        assertEquals(raw.planeradTill, result.planeradTill);
        assertEquals(raw.utford, result.utford);
        assertSame(raw.individer, result.individer);
        assertEquals(raw.regel, result.regel);
        assertEquals(raw.beskrivning, result.beskrivning);
        assertEquals(raw.verksamhetslogik, result.verksamhetslogik);
        assertEquals(raw.roll, result.roll);
        assertEquals(raw.url, result.url);
    }

    @Test
    void transform_nullPlaneradTill_defaultsToEmptyString() {
        RawOperativUppgift raw = fullRaw();
        raw.planeradTill = null;

        OperativUppgift result = UppgiftMapper.transform(raw);

        assertEquals("", result.planeradTill);
    }

    @Test
    void transform_nullUtford_defaultsToEmptyString() {
        RawOperativUppgift raw = fullRaw();
        raw.utford = null;

        OperativUppgift result = UppgiftMapper.transform(raw);

        assertEquals("", result.utford);
    }

    @Test
    void transform_bothNullDates_bothDefaultToEmptyString() {
        RawOperativUppgift raw = fullRaw();
        raw.planeradTill = null;
        raw.utford = null;

        OperativUppgift result = UppgiftMapper.transform(raw);

        assertEquals("", result.planeradTill);
        assertEquals("", result.utford);
    }
}
