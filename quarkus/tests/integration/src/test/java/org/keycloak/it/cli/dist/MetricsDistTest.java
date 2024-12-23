/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.it.cli.dist;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.keycloak.it.junit5.extension.DistributionTest;
import org.keycloak.it.utils.KeycloakDistribution;

import io.quarkus.test.junit.main.Launch;

@DistributionTest(keepAlive = true,
        requestPort = 9000,
        containerExposedPorts = {8080, 9000})
@Tag(DistributionTest.SLOW)
public class MetricsDistTest {

    @Test
    @Launch({ "start-dev" })
    void testMetricsEndpointNotEnabled(KeycloakDistribution distribution) {
        assertThrows(IOException.class, () -> when().get("/metrics"), "Connection refused must be thrown");
        assertThrows(IOException.class, () -> when().get("/q/metrics"), "Connection refused must be thrown");

        distribution.setRequestPort(8080);

        when().get("/metrics").then()
                .statusCode(404);
        when().get("/q/metrics").then()
                .statusCode(404);
    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true" })
    void testMetricsEndpoint() {
        when().get("/metrics").then()
                .statusCode(200)
                .body(containsString("jvm_gc_"))
                .body(containsString("http_server_active_requests"))
                .body(containsString("vendor_statistics_hit_ratio"))
                .body(not(containsString("vendor_statistics_miss_times_seconds_bucket")));

        when().get("/health").then()
                .statusCode(404);
    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true", "--cache-metrics-histograms-enabled=true", "--http-metrics-slos=5,10,25,50,250,500", "--http-metrics-histograms-enabled=true" })
    void testMetricsEndpointWithCacheMetricsHistograms() {
        when().get("/metrics").then()
                .statusCode(200)
                .body(containsString("vendor_statistics_miss_times_seconds_bucket"));

        // histograms are only available at the second request as they then contain the metrics of the first request
        when().get("/metrics").then()
                .statusCode(200)
                .body(containsString("http_server_requests_seconds_bucket{method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/metrics\",le=\"0.005\"}"))
                .body(containsString("http_server_requests_seconds_bucket{method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/metrics\",le=\"0.005592405\"}"));

    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true", "--features=user-event-metrics", "--event-metrics-user-enabled=true" })
    void testMetricsEndpointWithUserEventMetrics(KeycloakDistribution distribution) {
        runClientCredentialGrantWithUnknownClientId(distribution);

        distribution.setRequestPort(9000);
        when().get("/metrics").then()
                .statusCode(200)
                .body(containsString("keycloak_user_events_total{error=\"client_not_found\",event=\"client_login\",realm=\"master\"}"));

    }

    @Test
    @Launch({ "start-dev", "--metrics-enabled=true", "--features=user-event-metrics", "--event-metrics-user-enabled=false" })
    void testMetricsEndpointWithoutUserEventMetrics(KeycloakDistribution distribution) {
        runClientCredentialGrantWithUnknownClientId(distribution);

        distribution.setRequestPort(9000);
        when().get("/metrics").then()
                .statusCode(200)
                .body(not(containsString("keycloak_user_events_total{error=\"client_not_found\",event=\"client_login\",realm=\"master\"}")));

    }

    private static void runClientCredentialGrantWithUnknownClientId(KeycloakDistribution distribution) {
        distribution.setRequestPort(8080);
        given().formParam("grant_type", "client_credentials")
                .formParam("client_id", "unknown")
                .formParam("client_secret", "unknown").
                when().post("/realms/master/protocol/openid-connect/token")
                .then()
                .statusCode(401);
    }

    @Test
    void testUsingRelativePath(KeycloakDistribution distribution) {
        for (String relativePath : List.of("/auth", "/auth/", "auth")) {
            distribution.run("start-dev", "--metrics-enabled=true", "--http-management-relative-path=" + relativePath);
            if (!relativePath.endsWith("/")) {
                relativePath = relativePath + "/";
            }
            when().get(relativePath + "metrics").then().statusCode(200);
            distribution.stop();
        }
    }

    @Test
    void testMultipleRequests(KeycloakDistribution distribution) throws Exception {
        for (String relativePath : List.of("/", "/auth/", "auth")) {
            distribution.run("start-dev", "--metrics-enabled=true", "--http-management-relative-path=" + relativePath);
            CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

            for (int i = 0; i < 3; i++) {
                future = CompletableFuture.allOf(CompletableFuture.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 200; i++) {
                            String metricsPath = "metrics";

                            if (!relativePath.endsWith("/")) {
                                metricsPath = "/" + metricsPath;
                            }

                            when().get(relativePath + metricsPath).then().statusCode(200);
                        }
                    }
                }), future);
            }

            future.get(5, TimeUnit.MINUTES);

            distribution.stop();
        }
    }

}
