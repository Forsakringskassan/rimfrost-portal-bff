package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Body for OUL POST /uppgifter/individ/unassignable (oul-openapi 2.4.1). The personnummer goes
// in the body so it stays out of URLs and access logs; never log this object.
public class IndividSearchBackendRequest
{
   @JsonProperty("id_typ")
   public String idTyp;

   @JsonProperty("id_varde")
   public String idVarde;

   @JsonProperty("statuses")
   public List<String> statuses;

   public IndividSearchBackendRequest()
   {
   }

   public IndividSearchBackendRequest(String idTyp, String idVarde, List<String> statuses)
   {
      this.idTyp = idTyp;
      this.idVarde = idVarde;
      this.statuses = statuses;
   }
}
