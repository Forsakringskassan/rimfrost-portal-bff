package se.fk.github.portalbff;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import se.fk.github.portalbff.integration.OulClient;
import se.fk.github.portalbff.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortalBffController
{

   private static final Logger LOGGER = LoggerFactory.getLogger(PortalBffController.class);

   private static String readUpstreamBody(WebApplicationException e)
   {
      try
      {
         Response r = e.getResponse();
         r.bufferEntity();
         return r.readEntity(String.class);
      }
      catch (Exception ignored)
      {
         return "";
      }
   }

   @Inject
   @RestClient
   OulClient oulClient;

   @Inject
   SidService sidService;

   @ConfigProperty(name = "portal.mock.handlaggare", defaultValue = "false")
   boolean mockHandlaggare;

   // Read REMOTES_CONFIG_PATH from the env, falls back to empty (uses bundled file)
   @ConfigProperty(name = "portal.remotes.config.path", defaultValue = "")
   Optional<String> remotesConfigPath;

   // GET /api/route-manifest
   // Reads remotes.json either from a mounted ConfigMap path (Kubernetes)
   // or falls back to the bundled file in src/main/resources
   @GET
   @Path("/api/route-manifest")
   public Response routeManifest()
   {
      LOGGER.debug("GET /api/route-manifest");
      try
      {
         String json;
         if (remotesConfigPath.isPresent() && !remotesConfigPath.get().isBlank())
         {
            json = Files.readString(Paths.get(remotesConfigPath.get()), StandardCharsets.UTF_8);
         }
         else
         {
            var stream = getClass().getClassLoader().getResourceAsStream("remotes.json");
            if (stream == null)
            {
               LOGGER.error("remotes.json not found on classpath");
               return Response.status(500).entity(Map.of("error", "remotes.json not found")).build();
            }
            json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
         }
         return Response.ok(json).type(MediaType.APPLICATION_JSON).build();
      }
      catch (IOException e)
      {
         LOGGER.error("Failed to read remotes config from path={}", remotesConfigPath.orElse("classpath"), e);
         return Response.status(500).entity(Map.of("error", "Failed to read remotes config")).build();
      }
   }

   // GET /handlaggare
   // Returns hardcoded mock data - matches the TODO state in the Typescript BFF
   @GET
   @Path("/handlaggare")
   public Response getHandlaggare()
   {
      if (mockHandlaggare)
      {
         HandlaggarId id1 = new HandlaggarId();
         id1.typId = "card";
         id1.varde = "a1a1a1a1-0000-0000-0000-000000000001";

         HandlaggarId id2 = new HandlaggarId();
         id2.typId = "card";
         id2.varde = "a1a1a1a1-0000-0000-0000-000000000002";

         HandlaggarId id3 = new HandlaggarId();
         id3.typId = "card";
         id3.varde = "a1a1a1a1-0000-0000-0000-000000000003";

         Handlaggare h1 = new Handlaggare();
         h1.handlaggarId = id1;
         h1.fornamn = "Lisa";
         h1.efternamn = "Tass";

         Handlaggare h2 = new Handlaggare();
         h2.handlaggarId = id2;
         h2.fornamn = "Karl";
         h2.efternamn = "von Dobermann";

         Handlaggare h3 = new Handlaggare();
         h3.handlaggarId = id3;
         h3.fornamn = "Åsa";
         h3.efternamn = "Ormsäter";

         return Response.ok(Map.of("handlaggare", List.of(h1, h2, h3))).build();
      }
      else
      {
         return Response.status(503).entity(Map.of("error", "Handlaggare data is not available")).build();
      }
   }

   // POST /tasks
   // Fetches all tasks for a handler from OUL and transforms them
   @POST
   @Path("/tasks")
   public Response getTasks(@Valid TasksRequest body, @HeaderParam("Authorization") String authorization)
   {
      MDC.put("typId", body.typId);
      LOGGER.info("POST /tasks - Authorization: {}",
            authorization != null ? "present, length=" + authorization.length() : "null");
      try
      {
         RawTaskBackendResponse raw = oulClient.getTasks(authorization);
         List<OperativUppgift> transformed = raw.operativaUppgifter == null
               ? List.of()
               : raw.operativaUppgifter.stream()
                     .filter(u -> !sidService.hasSid(u.individer))
                     .map(UppgiftMapper::transform)
                     .toList();

         TasksResponse result = new TasksResponse();
         result.operativaUppgifter = transformed;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for typId={}: {}", e.getResponse().getStatus(), body.typId, upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error", "upstream", upstream))
               .build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch tasks for typId={}, OUL unreachable", body.typId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch tasks for typId={}", body.typId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("typId");
      }
   }

   // GET /tasks/team
   // Fetches all tasks assigned to the handler's team from OUL
   @GET
   @Path("/tasks/team")
   public Response getTeamTasks(@HeaderParam("Authorization") String authorization)
   {
      try
      {
         RawTaskBackendResponse raw = oulClient.getTeamTasks(authorization);
         List<OperativUppgift> transformed = raw.operativaUppgifter == null
               ? List.of()
               : raw.operativaUppgifter.stream()
                     .filter(u -> !sidService.hasSid(u.individer))
                     .map(UppgiftMapper::transform)
                     .toList();

         TasksResponse result = new TasksResponse();
         result.operativaUppgifter = transformed;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for team tasks: {}", e.getResponse().getStatus(), upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error", "upstream", upstream))
               .build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch team tasks, OUL unreachable", e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch team tasks", e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
   }

   // POST /tasks/{uppgiftId}/reassign
   // Reassigns a task to the calling handler from OUL
   @POST
   @Path("/tasks/{uppgiftId}/reassign")
   public Response reassignTask(@PathParam("uppgiftId") String uppgiftId, @HeaderParam("Authorization") String authorization)
   {
      MDC.put("uppgiftId", uppgiftId);
      try
      {
         RawGetNextBackendResponse raw = oulClient.reassignTask(uppgiftId, authorization);
         if (raw.operativUppgift != null && sidService.hasSid(raw.operativUppgift.individer))
         {
            LOGGER.warn("Reassigned task has SID individ, denying access");
            return Response.status(403).build();
         }
         OperativUppgift transformed = raw.operativUppgift != null
               ? UppgiftMapper.transform(raw.operativUppgift)
               : null;

         GetNextResponse result = new GetNextResponse();
         result.uppgift = transformed;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for reassign uppgiftId={}: {}", e.getResponse().getStatus(), uppgiftId, upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error", "upstream", upstream))
               .build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to reassign task uppgiftId={}, OUL unreachable", uppgiftId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to reassign task uppgiftId={}", uppgiftId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("uppgiftId");
      }
   }

   // POST /tasks/getNext
   // Assigns a new task to a handler from OUL and transforms the result
   @POST
   @Path("/tasks/getNext")
   public Response getNextTask(@Valid TasksRequest body, @HeaderParam("Authorization") String authorization)
   {
      MDC.put("typId", body.typId);
      try
      {
         RawGetNextBackendResponse raw = oulClient.assignTask(authorization);
         if (raw.operativUppgift != null && sidService.hasSid(raw.operativUppgift.individer))
         {
            LOGGER.warn("Assigned task has SID individ, denying access");
            return Response.status(403).build();
         }
         OperativUppgift transformed = raw.operativUppgift != null
               ? UppgiftMapper.transform(raw.operativUppgift)
               : null;

         GetNextResponse result = new GetNextResponse();
         result.uppgift = transformed;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for getNext typId={}: {}", e.getResponse().getStatus(), body.typId, upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error", "upstream", upstream))
               .build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to assign task for typId={}, OUL unreachable", body.typId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to assign task for typId={}", body.typId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("typId");
      }
   }
}
