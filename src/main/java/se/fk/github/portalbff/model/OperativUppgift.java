package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public class OperativUppgift
{
   public String uppgiftId;
   public String handlaggningId;
   public String skapad;
   public String status;
   public HandlaggarId handlaggarId;
   public String planeradTill;
   public String utford;
   public List<HandlaggarId> individer;
   public String regel;
   public String beskrivning;
   public String verksamhetslogik;
   public String roll;
   public String url;
   // Optional: absent for tasks not created in a process activity or for processes without a processvy
   @JsonInclude(JsonInclude.Include.NON_NULL)
   public String aktivitetId;
   @JsonInclude(JsonInclude.Include.NON_NULL)
   public String processvyUrl;
}
