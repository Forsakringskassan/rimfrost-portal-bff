package se.fk.github.portalbff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TasksResponse {
    @JsonProperty("operativa_uppgifter")
    public List<OperativUppgift> operativaUppgifter;
}