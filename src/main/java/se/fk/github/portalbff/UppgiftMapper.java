package se.fk.github.portalbff;

import se.fk.github.portalbff.model.OperativUppgift;
import se.fk.github.portalbff.model.RawOperativUppgift;

public class UppgiftMapper
{

   public static OperativUppgift transform(RawOperativUppgift raw)
   {
      OperativUppgift result = new OperativUppgift();
      result.uppgiftId = raw.uppgiftId;
      result.handlaggningId = raw.handlaggningId;
      result.skapad = raw.skapad;
      result.status = raw.status;
      result.handlaggarId = raw.handlaggarId;
      result.planeradTill = raw.planeradTill != null ? raw.planeradTill : "";
      result.utford = raw.utford != null ? raw.utford : "";
      result.individer = raw.individer;
      result.regel = raw.regel;
      result.beskrivning = raw.beskrivning;
      result.verksamhetslogik = raw.verksamhetslogik;
      result.roll = raw.roll;
      result.url = raw.url;
      return result;
   }
}
