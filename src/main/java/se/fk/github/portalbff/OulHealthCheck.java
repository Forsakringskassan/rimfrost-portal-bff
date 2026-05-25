package se.fk.github.portalbff;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.net.HttpURLConnection;
import java.net.URL;

@Readiness
@ApplicationScoped
public class OulHealthCheck implements HealthCheck
{

   @ConfigProperty(name = "quarkus.rest-client.oul.url")
   String oulUrl;

   @Override
   public HealthCheckResponse call()
   {
      try
      {
         HttpURLConnection connection = (HttpURLConnection) new URL(oulUrl).openConnection();
         connection.setConnectTimeout(2000);
         connection.setReadTimeout(2000);
         connection.setRequestMethod("HEAD");
         int status = connection.getResponseCode();
         if (status < 500)
         {
            return HealthCheckResponse.up("oul-backend");
         }
         return HealthCheckResponse.named("oul-backend").down()
               .withData("status", status)
               .build();
      }
      catch (Exception e)
      {
         return HealthCheckResponse.named("oul-backend").down()
               .withData("error", e.getMessage())
               .build();
      }
   }
}
