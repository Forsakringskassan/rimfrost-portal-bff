package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchTasksRequest
{
   // Sent in the body rather than the URL so it never ends up in access logs (PBFF-FR-05.1).
   // Must not be logged anywhere in the BFF either.
   @JsonProperty("personnummer")
   public String personnummer;
}
