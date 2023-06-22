package org.keycloak.device;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.account.DeviceRepresentation;

import jakarta.ws.rs.core.HttpHeaders;

public class DeviceRepresentationProviderImpl implements DeviceRepresentationProvider {
    private static final Logger logger = Logger.getLogger(DeviceActivityManager.class);
    private static final int USER_AGENT_MAX_LENGTH = 512;


    private final KeycloakSession session;

    DeviceRepresentationProviderImpl(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public DeviceRepresentation deviceRepresentation() {
        KeycloakContext context = session.getContext();

        if (context.getRequestHeaders() == null) {
            return null;
        }

        String userAgent = context.getRequestHeaders().getHeaderString(HttpHeaders.USER_AGENT);

        if (userAgent == null) {
            return null;
        }

        if (userAgent.length() > USER_AGENT_MAX_LENGTH) {
            logger.warn("Ignoring User-Agent header. Length is above the permitted: " + USER_AGENT_MAX_LENGTH);
            return null;
        }

        DeviceRepresentation current;
        try {
            current = new DeviceRepresentation();

            current.setDevice("device");
            current.setBrowser("browser");
            current.setOs("os");
            current.setOsVersion("osVersion");
            current.setIpAddress(context.getConnection().getRemoteAddr());
            current.setMobile(userAgent.toLowerCase().contains("mobile"));
        } catch (Exception cause) {
            logger.error("Failed to create device info from user agent header", cause);
            return null;
        }
        return current;
    }
}
