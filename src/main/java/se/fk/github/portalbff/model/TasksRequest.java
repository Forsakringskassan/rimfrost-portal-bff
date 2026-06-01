package se.fk.github.portalbff.model;

import jakarta.validation.constraints.NotBlank;

public class TasksRequest
{
   @NotBlank
   public String typId;

   @NotBlank
   public String varde;
}
