package org.keycloak.test.framework.server;

import io.quarkus.maven.dependency.Dependency;
import org.jboss.logging.Logger;

import java.net.ConnectException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RemoteKeycloakTestServer implements KeycloakTestServer {

    private static final Logger LOGGER = Logger.getLogger(RemoteKeycloakTestServer.class);

    @Override
    public void start(List<String> rawOptions, Set<Dependency> dependencies) {
        if (!verifyRunningKeycloak()) {
            long waitUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);

            LOGGER.warnv("Keycloak not running, please start Keycloak on {0}", getBaseUrl());
            LOGGER.infov("Requested server config: {0}", String.join(" ", rawOptions));
            LOGGER.infov("Requested dependencies: {0}", String.join(" ", dependencies.toString()));
            LOGGER.infov("Waiting for Keycloak to start");

            while (!verifyRunningKeycloak() && System.currentTimeMillis() < waitUntil) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public String getBaseUrl() {
        return "http://localhost:8080";
    }

    private boolean verifyRunningKeycloak() {
        try {
            new URL(getBaseUrl()).openConnection().connect();
            System.out.println("Connected");
            return true;
        } catch (ConnectException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
