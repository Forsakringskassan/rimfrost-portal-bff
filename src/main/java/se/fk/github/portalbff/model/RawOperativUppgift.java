package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RawOperativUppgift
{
   @JsonProperty("uppgift_id")
   public String uppgiftId;
   @JsonProperty("handlaggning_id")
   public String handlaggningId;
   @JsonProperty("skapad")
   public String skapad;
   @JsonProperty("status")
   public String status;
   @JsonProperty("handlaggar_id")
   public HandlaggarId handlaggarId;
   @JsonProperty("planerad_till")
   public String planeradTill;
   @JsonProperty("utford")
   public String utford;
   @JsonProperty("individer")
   public List<HandlaggarId> individer;
   @JsonProperty("regel")
   public String regel;
   @JsonProperty("beskrivning")
   public String beskrivning;
   @JsonProperty("verksamhetslogik")
   public String verksamhetslogik;
   @JsonProperty("roll")
   public String roll;
   @JsonProperty("url")
   public String url;
}
