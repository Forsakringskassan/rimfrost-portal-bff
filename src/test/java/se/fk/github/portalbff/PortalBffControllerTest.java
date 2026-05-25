package se.fk.github.portalbff;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(WireMockTestResource.class)
class PortalBffControllerTest {

    @BeforeEach
    void setUp() {
        WireMockTestResource.getServer().resetAll();
    }

    @Test
    void getTasks_returnsMappedTasks() {
        WireMockTestResource.getServer().stubFor(get(urlMatching("/uppgifter/handlaggare/.*"))
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
                        ]
                    }
                    """)));

        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(200)
            .body("operativaUppgifter", hasSize(1))
            .body("operativaUppgifter[0].uppgiftId", equalTo("task-1"))
            .body("operativaUppgifter[0].status", equalTo("AKTIV"))
            .body("operativaUppgifter[0].planeradTill", equalTo("2024-02-01"));
    }

    @Test
    void getTasks_emptyList_whenOulReturnsNoTasks() {
        WireMockTestResource.getServer().stubFor(get(urlMatching("/uppgifter/handlaggare/.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"operativa_uppgifter\": null}")));

        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(200)
            .body("operativaUppgifter", empty());
    }

    @Test
    void getTasks_returns400_whenTypIdIsBlank() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(400);
    }

    @Test
    void getTasks_returns400_whenVardeIsBlank() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(400);
    }

    @Test
    void getTasks_returns500_whenOulFails() {
        WireMockTestResource.getServer().stubFor(get(urlMatching("/uppgifter/handlaggare/.*"))
            .willReturn(aResponse().withStatus(500)));

        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"type-1\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks")
            .then()
            .statusCode(500)
            .body("error", equalTo("Internal server error"));
    }

    @Test
    void getNextTask_returnsMappedTask() {
        WireMockTestResource.getServer().stubFor(post(urlMatching("/uppgifter/handlaggare/.*"))
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
    void getNextTask_returns400_whenTypIdIsBlank() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"typId\": \"\", \"varde\": \"value-1\"}")
            .when()
            .post("/tasks/getNext")
            .then()
            .statusCode(400);
    }
}
