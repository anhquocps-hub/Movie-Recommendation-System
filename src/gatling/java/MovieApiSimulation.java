import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class MovieApiSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder browseMovies = scenario("Browse Movies")
            .exec(http("List Movies")
                    .get("/api/v1/movies?page=0&size=10")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("List Genres")
                    .get("/api/v1/genres")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Search Movies")
                    .get("/api/v1/movies/search?keyword=dark&page=0&size=10")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Get Movie by Slug")
                    .get("/api/v1/movies/slug/inception")
                    .check(status().is(200)));

    ScenarioBuilder authenticatedFlow = scenario("Authenticated User Flow")
            .exec(http("Login")
                    .post("/api/v1/auth/login")
                    .body(StringBody("{\"email\":\"alice@test.com\",\"password\":\"password\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.data.accessToken").saveAs("token")))
            .pause(1)
            .exec(http("Get Recommendations")
                    .get("/api/v1/recommendations")
                    .header("Authorization", "Bearer #{token}")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Get Watchlist")
                    .get("/api/v1/watchlist")
                    .header("Authorization", "Bearer #{token}")
                    .check(status().is(200)))
            .pause(1)
            .exec(http("Get Notifications")
                    .get("/api/v1/notifications")
                    .header("Authorization", "Bearer #{token}")
                    .check(status().is(200)));

    ScenarioBuilder healthCheck = scenario("Health Check")
            .exec(http("Actuator Health")
                    .get("/actuator/health")
                    .check(status().is(200)));

    {
        setUp(
                browseMovies.injectOpen(
                        rampUsers(50).during(Duration.ofSeconds(30)),
                        constantUsersPerSec(10).during(Duration.ofSeconds(30))
                ),
                authenticatedFlow.injectOpen(
                        rampUsers(20).during(Duration.ofSeconds(30))
                ),
                healthCheck.injectOpen(
                        constantUsersPerSec(2).during(Duration.ofSeconds(60))
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile3().lt(2000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
