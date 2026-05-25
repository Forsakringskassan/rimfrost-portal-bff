package se.fk.github.portalbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.portalbff.model.RawTaskBackendResponse;
import se.fk.github.portalbff.model.RawGetNextBackendResponse;
import se.fk.github.portalbff.model.TasksRequest;

@RegisterRestClient(configKey = "oul")
@Path("/uppgifter/handlaggare")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface OulClient
{
   @GET
   @Path("/{typId}/{varde}")
   RawTaskBackendResponse getTasks(
         @PathParam("typId") String typId,
         @PathParam("varde") String varde);

   @POST
   @Path("/{typId}/{varde}")
   RawGetNextBackendResponse assignTask(
         @PathParam("typId") String typId,
         @PathParam("varde") String varde,
         TasksRequest body);
}
