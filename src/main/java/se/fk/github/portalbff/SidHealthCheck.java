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
public class SidHealthCheck implements HealthCheck
{

   @ConfigProperty(name = "quarkus.rest-client.sid.url")
   String sidUrl;

   @Override
   public HealthCheckResponse call()
   {
      HttpURLConnection connection = null;
      try
      {
         connection = (HttpURLConnection) new URL(sidUrl).openConnection();
         connection.setConnectTimeout(2000);
         connection.setReadTimeout(2000);
         connection.setRequestMethod("HEAD");
         int status = connection.getResponseCode();
         if (status < 500)
         {
            return HealthCheckResponse.up("sid-backend");
         }
         return HealthCheckResponse.named("sid-backend").down()
               .withData("status", status)
               .build();
      }
      catch (Exception e)
      {
         return HealthCheckResponse.named("sid-backend").down()
               .withData("error", e.getMessage())
               .build();
      }
      finally
      {
         if (connection != null)
         {
            connection.disconnect();
         }
      }
   }
}
