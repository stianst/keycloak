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

package org.keycloak.quarkus.runtime.storage.legacy.database;

import io.quarkus.arc.Arc;
import jakarta.enterprise.inject.Instance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.jboss.logging.Logger;
import org.keycloak.common.Profile;
import org.keycloak.connections.jpa.DefaultJpaConnectionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.connections.jpa.util.JpaUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.quarkus.runtime.storage.database.jpa.AbstractJpaConnectionProviderFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.keycloak.connections.jpa.util.JpaUtils.configureNamedQuery;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LegacyJpaConnectionProviderFactory extends AbstractJpaConnectionProviderFactory implements ServerInfoAwareProviderFactory,
        EnvironmentDependentProviderFactory {

    public static final String QUERY_PROPERTY_PREFIX = "kc.query.";
    private static final Logger logger = Logger.getLogger(LegacyJpaConnectionProviderFactory.class);
    private static final String SQL_GET_LATEST_VERSION = "SELECT ID, VERSION FROM %sMIGRATION_MODEL ORDER BY UPDATE_TIME DESC";

    enum MigrationStrategy {
        UPDATE, VALIDATE, MANUAL
    }

    private Map<String, String> operationalInfo;

    @Override
    public JpaConnectionProvider create(KeycloakSession session) {
        logger.trace("Create QuarkusJpaConnectionProvider");
        return new DefaultJpaConnectionProvider(createEntityManager(entityManagerFactory, session));
    }

    @Override
    public String getId() {
        return "legacy";
    }

    private void addSpecificNamedQueries(KeycloakSession session) {
        EntityManager em = createEntityManager(entityManagerFactory, session);

        try {
            Map<String, Object> unitProperties = entityManagerFactory.getProperties();

            for (Map.Entry<String, Object> entry : unitProperties.entrySet()) {
                if (entry.getKey().startsWith(QUERY_PROPERTY_PREFIX)) {
                    configureNamedQuery(entry.getKey().substring(QUERY_PROPERTY_PREFIX.length()), entry.getValue().toString(), em);
                }
            }
        } finally {
            JpaUtils.closeEntityManager(em);
        }
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        super.postInit(factory);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("initializeEmpty")
                .type("boolean")
                .helpText("Initialize database if empty. If set to false the database has to be manually initialized. If you want to manually initialize the database set migrationStrategy to manual which will create a file with SQL commands to initialize the database.")
                .defaultValue(true)
                .add()
                .property()
                .name("migrationStrategy")
                .type("string")
                .helpText("Strategy to use to migrate database. Valid values are update, manual and validate. Update will automatically migrate the database schema. Manual will export the required changes to a file with SQL commands that you can manually execute on the database. Validate will simply check if the database is up-to-date.")
                .options("update", "manual", "validate")
                .defaultValue("update")
                .add()
                .property()
                .name("migrationExport")
                .type("string")
                .helpText("Path for where to write manual database initialization/migration file.")
                .add()
                .build();
    }

    @Override
    protected EntityManagerFactory getEntityManagerFactory() {
        Instance<EntityManagerFactory> instance = Arc.container().select(EntityManagerFactory.class);

        if (instance.isResolvable()) {
            return instance.get();
        }

        return getEntityManagerFactory("keycloak-default").orElseThrow(() -> new IllegalStateException("Failed to resolve the default entity manager factory"));
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return operationalInfo;
    }

    @Override
    public int order() {
        return 100;
    }


    private String getSchema(String schema) {
        return schema == null ? "" : schema + ".";
    }

    private File getDatabaseUpdateFile() {
        String databaseUpdateFile = config.get("migrationExport", "keycloak-database-update.sql");
        return new File(databaseUpdateFile);
    }

    private void createOperationalInfo(Connection connection) {
        try {
            operationalInfo = new LinkedHashMap<>();
            DatabaseMetaData md = connection.getMetaData();
            operationalInfo.put("databaseUrl", md.getURL());
            operationalInfo.put("databaseUser", md.getUserName());
            operationalInfo.put("databaseProduct", md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            operationalInfo.put("databaseDriver", md.getDriverName() + " " + md.getDriverVersion());
            logger.debugf("Database info: %s", operationalInfo.toString());
        } catch (SQLException e) {
            logger.warn("Unable to prepare operational info due database exception: " + e.getMessage());
        }
    }

    @Override
    public boolean isSupported() {
        return !Profile.isFeatureEnabled(Profile.Feature.MAP_STORAGE);
    }
}
