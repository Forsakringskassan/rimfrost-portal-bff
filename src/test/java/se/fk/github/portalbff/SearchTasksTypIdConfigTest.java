package se.fk.github.portalbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

// PBFF-FR-05.7: the id_typ sent to OUL comes from configuration, not from code.
@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
@TestProfile(SearchTasksTypIdConfigTest.CustomTypId.class)
class SearchTasksTypIdConfigTest
{

   public static class CustomTypId implements QuarkusTestProfile
   {
      @Override
      public Map<String, String> getConfigOverrides()
      {
         return Map.of("portal.oul.personnummer-typ-id", "personnummer");
      }
   }

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   @Test
   void searchTasks_usesConfiguredTypId()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/individ/personnummer/19900101-9999"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"operativa_uppgifter\": []}")));

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(200);

      WireMockTestResource.getServer().verify(
            getRequestedFor(urlPathEqualTo("/uppgifter/individ/personnummer/19900101-9999")));
   }
}
