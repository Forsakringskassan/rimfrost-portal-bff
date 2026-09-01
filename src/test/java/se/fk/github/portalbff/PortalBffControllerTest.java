package se.fk.github.portalbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class PortalBffControllerTest
{

   @BeforeEach
   void setUp()
   {
      WireMockTestResource.getServer().resetAll();
   }

   @Test
   void getTasks_returnsMappedTasks()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/handlaggare"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                            "operativa_uppgifter": [
                                {
                                    "uppgift_id": "task-1",
                                    "handlaggning_id": "handling-1",
                                    "skapad": "2024-01-01",
                                    "status": "AKTIV",
                                    "planerad_till": "2024-02-01",
                                    "utford": "2024-01-15",
                                    "regel": "REGEL_A",
                                    "beskrivning": "Test task",
                                    "verksamhetslogik": "VL",
                                    "roll": "HANDLAGGARE",
                                    "url": "http://example.com/task/1"
                                }
                            ],
                            "borttagna_pga_behorighet": 2
                        }
                        """)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(200)
            .body("operativa_uppgifter", hasSize(1))
            .body("operativa_uppgifter[0].uppgiftId", equalTo("task-1"))
            .body("operativa_uppgifter[0].status", equalTo("AKTIV"))
            .body("operativa_uppgifter[0].planeradTill", equalTo("2024-02-01"))
            .body("borttagna_pga_behorighet", equalTo(2));
   }

   @Test
   void getTasks_emptyList_whenOulReturnsNoTasks()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/handlaggare"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"operativa_uppgifter\": null, \"borttagna_pga_behorighet\": 0}")));

      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(200)
            .body("borttagna_pga_behorighet", equalTo(0))
            .body("operativa_uppgifter", empty());
   }

   @Test
   void getTasks_returns400_whenTypIdIsBlank()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(400);
   }

   @Test
   void getTasks_returns500_whenOulFails()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/handlaggare"))
            .willReturn(aResponse().withStatus(500)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"))
            .body("$", not(hasKey("upstream")));
   }

   @Test
   void getTasks_acceptsMissingVarde()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/handlaggare"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("{\"operativa_uppgifter\": null, \"borttagna_pga_behorighet\": 0}")));

      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(200);
   }

   @Test
   void getTeamTasks_forwardsBorttagnaPgaBehorighetUnchanged()
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo("/uppgifter/team"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                            "operativa_uppgifter": [],
                            "borttagna_pga_behorighet": 3
                        }
                        """)));

      given()
            .when()
            .get("/tasks/team")
            .then()
            .statusCode(200)
            .body("borttagna_pga_behorighet", equalTo(3));
   }

   @Test
   void reassignTask_returnsMappedTask()
   {
      WireMockTestResource.getServer().stubFor(post(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                            "operativ_uppgift": {
                                "uppgift_id": "uppgift-1",
                                "handlaggning_id": "handling-3",
                                "skapad": "2024-01-03",
                                "status": "TILLDELAD",
                                "planerad_till": null,
                                "utford": null,
                                "regel": "REGEL_C",
                                "beskrivning": "Reassigned task",
                                "verksamhetslogik": "VL",
                                "roll": "HANDLAGGARE",
                                "url": "http://example.com/task/3"
                            }
                        }
                        """)));

      given()
            .contentType(ContentType.JSON)
            .when()
            .post("/tasks/uppgift-1/reassign")
            .then()
            .statusCode(200)
            .body("uppgift.uppgiftId", equalTo("uppgift-1"))
            .body("uppgift.status", equalTo("TILLDELAD"));
   }

   @Test
   void reassignTask_returns403_whenOulRejects()
   {
      WireMockTestResource.getServer().stubFor(post(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
            .willReturn(aResponse().withStatus(403)));

      given()
            .contentType(ContentType.JSON)
            .when()
            .post("/tasks/uppgift-1/reassign")
            .then()
            .statusCode(403)
            .body("error", equalTo("Upstream error"))
            .body("$", not(hasKey("upstream")));
   }

   @Test
   void getNextTask_returnsMappedTask()
   {
      WireMockTestResource.getServer().stubFor(post(urlPathEqualTo("/uppgifter/handlaggare"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody("""
                        {
                            "operativ_uppgift": {
                                "uppgift_id": "next-task-1",
                                "handlaggning_id": "handling-2",
                                "skapad": "2024-01-02",
                                "status": "TILLDELAD",
                                "planerad_till": null,
                                "utford": null,
                                "regel": "REGEL_B",
                                "beskrivning": "Next task",
                                "verksamhetslogik": "VL",
                                "roll": "HANDLAGGARE",
                                "url": "http://example.com/task/2"
                            }
                        }
                        """)));

      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks/getNext")
            .then()
            .statusCode(200)
            .body("uppgift.uppgiftId", equalTo("next-task-1"))
            .body("uppgift.status", equalTo("TILLDELAD"))
            .body("uppgift.planeradTill", equalTo(""))
            .body("uppgift.utford", equalTo(""));
   }

   @Test
   void getNextTask_returns400_whenTypIdIsBlank()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks/getNext")
            .then()
            .statusCode(400);
   }
}
