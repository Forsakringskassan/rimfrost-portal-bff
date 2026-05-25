package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RawGetNextBackendResponse
{
   @JsonProperty("operativ_uppgift")
   public RawOperativUppgift operativUppgift;
}
