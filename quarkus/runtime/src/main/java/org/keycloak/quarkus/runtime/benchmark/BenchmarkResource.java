package org.keycloak.quarkus.runtime.benchmark;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.models.KeycloakSession;
import org.keycloak.quarkus.runtime.benchmark.tasks.BenchmarkTask;
import org.keycloak.quarkus.runtime.benchmark.tasks.ClientES256;
import org.keycloak.quarkus.runtime.benchmark.tasks.ClientRS256;
import org.keycloak.quarkus.runtime.benchmark.tasks.Hello;
import org.keycloak.quarkus.runtime.benchmark.tasks.Sessions;
import org.keycloak.quarkus.runtime.benchmark.tasks.SignES256;
import org.keycloak.quarkus.runtime.benchmark.tasks.SignRS256;

import java.util.HashMap;
import java.util.Map;

public class BenchmarkResource {

    private KeycloakSession session;

    public BenchmarkResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> batch(
            @DefaultValue("client-es256,client-rs256,sign-es256,sign-rs256,sessions") @QueryParam("tasks") String tasks,
            @DefaultValue("1000") @QueryParam("iterations") int interations) {
        Map<String, Long> results = new HashMap<>();

        for (String taskName : tasks.split(",")) {
            BenchmarkTask task = getTask(taskName);
            task.init(session);

            long start = System.currentTimeMillis();
            for (int i = 0; i < interations; i++) {
                task.run();
            }
            long end = System.currentTimeMillis();
            long time = end - start;

            task.close();

            results.put(task.getClass().getSimpleName(), time);
        }

        return results;
    }

    @GET
    @Path("/{task}")
    public Response task(@PathParam("task") String taskName) {
        BenchmarkTask task = getTask(taskName);

        task.init(session);
        Object result = task.run();
        task.close();
        MediaType mediaType = task.mediaType();

        if (result == null || mediaType == null) {
            return Response.ok().build();
        }  else {
            return Response.ok(result, mediaType).build();
        }
    }

    private BenchmarkTask getTask(String taskName) {
        switch (taskName) {
            case "hello": return new Hello();
            case "client-rs256": return new ClientRS256();
            case "client-es256": return new ClientES256();
            case "sign-rs256": return new SignRS256();
            case "sign-es256": return new SignES256();
            case "sessions": return new Sessions(session.getKeycloakSessionFactory());
        }
        throw new NotFoundException("Task " + taskName + " not found");
    }

}
