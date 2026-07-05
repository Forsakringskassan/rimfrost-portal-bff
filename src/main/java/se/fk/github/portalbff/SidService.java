package se.fk.github.portalbff;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.portalbff.model.HandlaggarId;
import se.fk.rimfrost.framework.sid.adapter.SidAdapter;
import se.fk.rimfrost.framework.sid.exception.SidException;
import se.fk.rimfrost.framework.sid.model.ImmutableIdtyp;
import se.fk.rimfrost.framework.sid.model.Idtyp;

import java.util.List;

@ApplicationScoped
public class SidService
{

   private static final Logger LOGGER = LoggerFactory.getLogger(SidService.class);

   @Inject
   SidAdapter sidAdapter;

   public boolean hasSid(List<HandlaggarId> individer)
   {
      if (individer == null || individer.isEmpty())
      {
         return false;
      }
      List<Idtyp> idtyper = individer.stream()
            .<Idtyp>map(h -> ImmutableIdtyp.builder().typId(h.typId).varde(h.varde).build())
            .toList();
      try
      {
         return sidAdapter.containsSid(idtyper);
      }
      catch (SidException e)
      {
         LOGGER.error("SID check failed for {} individer, treating task as SID (fail closed)", individer.size(), e);
         return true;
      }
   }
}
