package org.keycloak.testsuite.arquillian.containers;

import java.io.File;
import org.apache.commons.validator.routines.IntegerValidator;
import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 *
 * @author tkyjovsk
 */
public class InfinispanServerConfiguration implements ContainerConfiguration {

    private String infinispanHome;
    private String serverConfig;
    private Integer portOffset;
    private Integer managementPort;
    private String additionalParameters;

    @Override
    public void validate() throws ConfigurationException {
        if (infinispanHome == null) {
            throw new ConfigurationException("`infinispanHome` cannot be null");
        }
        if (!new File(infinispanHome).isDirectory()) {
            throw new ConfigurationException(String.format("`infinispanHome` is not a valid directory: '%s'", infinispanHome));
        }

        if (portOffset == null) {
            portOffset = 0;
        }
        if (!IntegerValidator.getInstance().isInRange(portOffset, 1000, 64535)) {
            throw new ConfigurationException(String.format("Invalid portOffset: %s", portOffset));
        }

        if (managementPort == null) {
            managementPort = 9990 + portOffset;
        }
        if (!IntegerValidator.getInstance().isInRange(managementPort, 1000, 65535)) {
            throw new ConfigurationException(String.format("Invalid managementPort: %s", managementPort));
        }

    }

    public String getInfinispanHome() {
        return infinispanHome;
    }

    public void setInfinispanHome(String infinispanHome) {
        this.infinispanHome = infinispanHome;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    public Integer getPortOffset() {
        return portOffset;
    }

    public void setPortOffset(Integer portOffset) {
        this.portOffset = portOffset;
    }

    public String getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public Integer getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(Integer managementPort) {
        this.managementPort = managementPort;
    }

}
