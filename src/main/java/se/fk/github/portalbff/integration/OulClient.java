package se.fk.github.portalbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.portalbff.model.RawTaskBackendResponse;
import se.fk.github.portalbff.model.RawGetNextBackendResponse;

@RegisterRestClient(configKey = "oul")
@Path("/uppgifter")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface OulClient
{
   @GET
   @Path("/handlaggare")
   RawTaskBackendResponse getTasks(@HeaderParam("Authorization") String authorization);

   @POST
   @Path("/handlaggare")
   RawGetNextBackendResponse assignTask(@HeaderParam("Authorization") String authorization);

   @GET
   @Path("/team")
   RawTaskBackendResponse getTeamTasks(@HeaderParam("Authorization") String authorization);

   @POST
   @Path("/{uppgift_id}/handlaggare")
   RawGetNextBackendResponse reassignTask(@PathParam("uppgift_id") String uppgiftId, @HeaderParam("Authorization") String authorization);
}
