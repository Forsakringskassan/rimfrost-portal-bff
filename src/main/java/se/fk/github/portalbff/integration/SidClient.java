package se.fk.github.portalbff.integration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import se.fk.github.portalbff.model.SidStatusRequest;
import se.fk.github.portalbff.model.SidStatusResponse;

@RegisterRestClient(configKey = "sid")
@Path("/sid")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SidClient
{
   @POST
   @Path("/status")
   SidStatusResponse getSidStatus(SidStatusRequest request);
}
