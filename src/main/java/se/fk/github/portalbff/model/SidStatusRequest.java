package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SidStatusRequest
{
   @JsonProperty("individer")
   public List<HandlaggarId> individer;
}
