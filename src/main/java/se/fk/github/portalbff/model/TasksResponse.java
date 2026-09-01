package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TasksResponse
{
   @JsonProperty("operativa_uppgifter")
   public List<OperativUppgift> operativaUppgifter;

   // Passed through from OUL unchanged — the BFF makes no SID/behörighet judgement of its own.
   @JsonProperty("borttagna_pga_behorighet")
   public int borttagnaPgaBehorighet;
}
