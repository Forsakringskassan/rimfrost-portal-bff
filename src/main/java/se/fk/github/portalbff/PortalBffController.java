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

   private static final String STATUS_NY = "Ny";

   // id_typ for personnummer in OUL's individ search. Read from config, never hardcoded: it is an
   // FK-internal reference data id that may not be known up front, and other identity types may
   // be added (PBFF-FR-05.7).
   @ConfigProperty(name = "portal.oul.personnummer-typ-id")
   String personnummerTypId;

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
         // typId matches rimfrost-service-team's hardcoded stub (HandlaggareIdentitet.TYP_ID);
         // varde values match its INDIVID_A/B/C so these mock handläggare resolve to real teams.
         HandlaggarId id1 = new HandlaggarId();
         id1.typId = "116759e4-18fd-4209-849c-90abbd257d22";
         id1.varde = "111111111";

         HandlaggarId id2 = new HandlaggarId();
         id2.typId = "116759e4-18fd-4209-849c-90abbd257d22";
         id2.varde = "222222222";

         HandlaggarId id3 = new HandlaggarId();
         id3.typId = "116759e4-18fd-4209-849c-90abbd257d22";
         id3.varde = "333333333";

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
      MDC.put("clientTypId", body.typId);
      try
      {
         RawTaskBackendResponse raw = oulClient.getTasks(authorization);
         List<OperativUppgift> transformed = raw.operativaUppgifter == null
               ? List.of()
               : raw.operativaUppgifter.stream().map(UppgiftMapper::transform).toList();

         TasksResponse result = new TasksResponse();
         result.operativaUppgifter = transformed;
         result.borttagnaPgaBehorighet = raw.borttagnaPgaBehorighet;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for clientTypId={} (unverified): {}", e.getResponse().getStatus(), body.typId, upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to fetch tasks for clientTypId={} (unverified), OUL unreachable", body.typId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to fetch tasks for clientTypId={} (unverified)", body.typId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("clientTypId");
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
               : raw.operativaUppgifter.stream().map(UppgiftMapper::transform).toList();

         TasksResponse result = new TasksResponse();
         result.operativaUppgifter = transformed;
         result.borttagnaPgaBehorighet = raw.borttagnaPgaBehorighet;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for team tasks: {}", e.getResponse().getStatus(), upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
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

   // POST /tasks/search
   // Finds tasks for an individ that are not handed out via the queue, so a handläggare can
   // assign one manually (PBFF-FR-05). The personnummer is in the body to keep it out of URLs
   // and access logs, and is never logged here — not even OUL's error body, which may echo it.
   @POST
   @Path("/tasks/search")
   public Response searchTasks(SearchTasksRequest body, @HeaderParam("Authorization") String authorization)
   {
      Optional<String> personnummer = Personnummer.normalize(body == null ? null : body.personnummer);
      if (personnummer.isEmpty())
      {
         return Response.status(400).entity(Map.of("error", "Invalid personnummer")).build();
      }
      try
      {
         RawTaskBackendResponse raw = oulClient.searchIndividTasks(personnummerTypId, personnummer.get(), false,
               authorization);
         // OUL decides what the handläggare may see (incl. SID); this only narrows the result to
         // tasks with status Ny, even if OUL returns more (PBFF-FR-05.4).
         List<OperativUppgift> transformed = raw.operativaUppgifter == null
               ? List.of()
               : raw.operativaUppgifter.stream()
                     .filter(u -> STATUS_NY.equals(u.status))
                     .map(UppgiftMapper::transform)
                     .toList();

         TasksResponse result = new TasksResponse();
         result.operativaUppgifter = transformed;
         // OUL's individ search has no such signal; kept for the same response shape (PBFF-FR-05.3).
         result.borttagnaPgaBehorighet = 0;
         return Response.ok(result).build();
      }
      catch (WebApplicationException e)
      {
         int upstreamStatus = e.getResponse().getStatus();
         LOGGER.error("OUL returned {} for individ task search", upstreamStatus);
         // 400 and 403 are meaningful to the client; any other OUL error is a server-side
         // problem from its point of view (PBFF-FR-05.5).
         int status = upstreamStatus == 400 || upstreamStatus == 403 || upstreamStatus >= 500
               ? upstreamStatus
               : 502;
         return Response.status(status).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         // Only the exception type: the message or stack trace may contain the request URI, which
         // carries the personnummer as a path segment (PBFF-NFR-02.2).
         LOGGER.error("Failed to search individ tasks, OUL unreachable: {}", e.getClass().getSimpleName());
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to search individ tasks: {}", e.getClass().getSimpleName());
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
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
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

   // POST /tasks/{uppgiftId}/unassign
   // Lets the calling handler hand a task back to the pool (OUL-FR-19).
   // Pure forwarding: OUL owns the check that the caller is the current assignee.
   @POST
   @Path("/tasks/{uppgiftId}/unassign")
   public Response unassignTask(@PathParam("uppgiftId") String uppgiftId,
         @HeaderParam("Authorization") String authorization)
   {
      MDC.put("uppgiftId", uppgiftId);
      try
      {
         oulClient.unassignTask(uppgiftId, authorization);
         return Response.noContent().build();
      }
      catch (WebApplicationException e)
      {
         String upstream = readUpstreamBody(e);
         LOGGER.error("OUL returned {} for unassign uppgiftId={}: {}", e.getResponse().getStatus(), uppgiftId,
               upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to unassign task uppgiftId={}, OUL unreachable", uppgiftId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to unassign task uppgiftId={}", uppgiftId, e);
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
      MDC.put("clientTypId", body.typId);
      try
      {
         RawGetNextBackendResponse raw = oulClient.assignTask(authorization);
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
         LOGGER.error("OUL returned {} for getNext clientTypId={} (unverified): {}", e.getResponse().getStatus(), body.typId,
               upstream);
         return Response.status(e.getResponse().getStatus()).entity(Map.of("error", "Upstream error")).build();
      }
      catch (ProcessingException e)
      {
         LOGGER.error("Failed to assign task for clientTypId={} (unverified), OUL unreachable", body.typId, e);
         return Response.status(502).entity(Map.of("error", "Upstream unavailable")).build();
      }
      catch (Exception e)
      {
         LOGGER.error("Failed to assign task for clientTypId={} (unverified)", body.typId, e);
         return Response.status(500).entity(Map.of("error", "Internal server error")).build();
      }
      finally
      {
         MDC.remove("clientTypId");
      }
   }
}
