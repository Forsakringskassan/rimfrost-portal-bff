package se.fk.github.portalbff;

import com.github.tomakehurst.wiremock.client.WireMock;
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

   private static final String INDIVID_SEARCH_PATH = "/uppgifter/individ/c5f2e2b4-9143-4160-8f4b-30c172f0ac05/19900101-9999";

   private static void stubIndividSearch(String body)
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo(INDIVID_SEARCH_PATH))
            .withQueryParam("assignable", WireMock.equalTo("false"))
            .willReturn(aResponse()
                  .withHeader("Content-Type", "application/json")
                  .withBody(body)));
   }

   private static void stubIndividSearchStatus(int status)
   {
      WireMockTestResource.getServer().stubFor(get(urlPathEqualTo(INDIVID_SEARCH_PATH))
            .willReturn(aResponse().withStatus(status)));
   }

   @Test
   void searchTasks_returnsMappedTasks()
   {
      stubIndividSearch("""
            {
                "operativa_uppgifter": [
                    {
                        "uppgift_id": "sok-1",
                        "handlaggning_id": "handling-1",
                        "skapad": "2026-10-01",
                        "status": "Ny",
                        "planerad_till": null,
                        "utford": null,
                        "regel": "REGEL_KOMMUNICERING",
                        "beskrivning": "Kommunicering",
                        "verksamhetslogik": "VL",
                        "roll": "HANDLAGGARE",
                        "url": "http://example.com/task/sok-1"
                    }
                ]
            }
            """);

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(200)
            .body("operativa_uppgifter", hasSize(1))
            .body("operativa_uppgifter[0].uppgiftId", equalTo("sok-1"))
            .body("operativa_uppgifter[0].status", equalTo("Ny"))
            .body("operativa_uppgifter[0].planeradTill", equalTo(""))
            .body("borttagna_pga_behorighet", equalTo(0));

      WireMockTestResource.getServer().verify(
            getRequestedFor(urlPathEqualTo(INDIVID_SEARCH_PATH))
                  .withQueryParam("assignable", WireMock.equalTo("false"))
                  .withHeader("Authorization", WireMock.equalTo("Bearer test-token")));
   }

   @Test
   void searchTasks_emptyList_whenOulReturnsNoTasks()
   {
      stubIndividSearch("{\"operativa_uppgifter\": []}");

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(200)
            .body("operativa_uppgifter", empty());
   }

   @Test
   void searchTasks_keepsOnlyTasksWithStatusNy()
   {
      stubIndividSearch("""
            {
                "operativa_uppgifter": [
                    { "uppgift_id": "ny", "handlaggning_id": "h-1", "skapad": "2026-10-01", "status": "Ny" },
                    { "uppgift_id": "tilldelad", "handlaggning_id": "h-2", "skapad": "2026-10-01", "status": "Tilldelad",
                      "handlaggar_id": { "typId": "t", "varde": "111111111" } },
                    { "uppgift_id": "avslutad", "handlaggning_id": "h-3", "skapad": "2026-10-01", "status": "Avslutad" },
                    { "uppgift_id": "avbruten", "handlaggning_id": "h-4", "skapad": "2026-10-01", "status": "Avbruten" }
                ]
            }
            """);

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(200)
            .body("operativa_uppgifter", hasSize(1))
            .body("operativa_uppgifter[0].uppgiftId", equalTo("ny"));
   }

   @Test
   void searchTasks_normalizesPersonnummerWithoutHyphen()
   {
      stubIndividSearch("{\"operativa_uppgifter\": []}");

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"199001019999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(200);

      WireMockTestResource.getServer().verify(getRequestedFor(urlPathEqualTo(INDIVID_SEARCH_PATH)));
   }

   @Test
   void searchTasks_returns400_withoutCallingOul_whenPersonnummerIsInvalid()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(400)
            .body("error", equalTo("Invalid personnummer"));

      WireMockTestResource.getServer().verify(0, getRequestedFor(urlPathMatching("/uppgifter/individ/.*")));
   }

   @Test
   void searchTasks_returns400_withoutCallingOul_whenPersonnummerIsMissing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(400);

      WireMockTestResource.getServer().verify(0, getRequestedFor(urlPathMatching("/uppgifter/individ/.*")));
   }

   @Test
   void searchTasks_returns400_whenOulRejectsRequest()
   {
      stubIndividSearchStatus(400);

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(400)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void searchTasks_returns403_whenOulDenies()
   {
      stubIndividSearchStatus(403);

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(403)
            .body("error", equalTo("Upstream error"))
            .body("$", not(hasKey("upstream")));
   }

   @Test
   void searchTasks_returns500_whenOulFails()
   {
      stubIndividSearchStatus(500);

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(500)
            .body("error", equalTo("Upstream error"));
   }

   @Test
   void searchTasks_returns502_whenOulReturnsOtherClientError()
   {
      stubIndividSearchStatus(404);

      given()
            .contentType(ContentType.JSON)
            .body("{\"personnummer\": \"19900101-9999\"}")
            .when()
            .post("/tasks/search")
            .then()
            .statusCode(502)
            .body("error", equalTo("Upstream error"));

      // WireMock answers 404 for unmatched requests too, so check the stub was actually hit.
      WireMockTestResource.getServer().verify(getRequestedFor(urlPathEqualTo(INDIVID_SEARCH_PATH)));
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
   void unassignTask_returns204_whenOulAccepts()
   {
      WireMockTestResource.getServer().stubFor(delete(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
            .willReturn(aResponse().withStatus(204)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer test-token")
            .when()
            .post("/tasks/uppgift-1/unassign")
            .then()
            .statusCode(204);

      // OUL derives the handläggare's identity from this token and uses it to check
      // ownership (OUL-FR-19.2, 19.3), so forwarding it is the point of the endpoint.
      WireMockTestResource.getServer().verify(
            deleteRequestedFor(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
                  .withHeader("Authorization", WireMock.equalTo("Bearer test-token")));
   }

   @Test
   void unassignTask_returns403_whenCallerIsNotAssignee()
   {
      WireMockTestResource.getServer().stubFor(delete(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
            .willReturn(aResponse().withStatus(403)));

      given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer other-handlaggare")
            .when()
            .post("/tasks/uppgift-1/unassign")
            .then()
            .statusCode(403)
            .body("error", equalTo("Upstream error"))
            .body("$", not(hasKey("upstream")));

      // The token has to reach OUL on the error path too — that is what lets OUL
      // decide the caller is not the assignee and answer 403 (OUL-FR-19.3).
      WireMockTestResource.getServer().verify(
            deleteRequestedFor(urlPathEqualTo("/uppgifter/uppgift-1/handlaggare"))
                  .withHeader("Authorization", WireMock.equalTo("Bearer other-handlaggare")));
   }

   @Test
   void unassignTask_returns404_whenUppgiftDoesNotExist()
   {
      WireMockTestResource.getServer().stubFor(delete(urlPathEqualTo("/uppgifter/saknas/handlaggare"))
            .willReturn(aResponse().withStatus(404)));

      given()
            .contentType(ContentType.JSON)
            .when()
            .post("/tasks/saknas/unassign")
            .then()
            .statusCode(404)
            .body("error", equalTo("Upstream error"));

      // WireMock answers 404 for any unmatched request, so without this the test would
      // also pass if the client used the wrong verb or path.
      WireMockTestResource.getServer().verify(
            deleteRequestedFor(urlPathEqualTo("/uppgifter/saknas/handlaggare")));
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
