package se.fk.github.portalbff;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter
{
   @ConfigProperty(name = "quarkus.http.cors.origins", defaultValue = "http://localhost:3000,http://localhost:3030")
   List<String> allowedOrigins;

   @Override
   public void filter(ContainerRequestContext req)
   {
      if (!"OPTIONS".equalsIgnoreCase(req.getMethod()))
      {
         return;
      }
      String origin = req.getHeaderString("Origin");
      if (origin != null && allowedOrigins.contains(origin))
      {
         req.abortWith(corsOkResponse(origin));
      }
   }

   @Override
   public void filter(ContainerRequestContext req, ContainerResponseContext res)
   {
      String origin = req.getHeaderString("Origin");
      if (origin != null && allowedOrigins.contains(origin))
      {
         res.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
         res.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
         res.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, Authorization");
      }
   }

   private Response corsOkResponse(String origin)
   {
      return Response.ok()
            .header("Access-Control-Allow-Origin", origin)
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
            .header("Access-Control-Max-Age", "86400")
            .build();
   }
}
