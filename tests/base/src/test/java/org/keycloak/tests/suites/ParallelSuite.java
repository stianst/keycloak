package org.keycloak.tests.suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.keycloak.tests.admin.AdminConsoleLandingPageTest;
import org.keycloak.tests.admin.AdminHeadersTest;
import org.keycloak.tests.admin.AdminPreflightTest;
import org.keycloak.tests.admin.AttackDetectionResourceTest;
import org.keycloak.tests.admin.CrossRealmPermissionsTest;
import org.keycloak.tests.admin.InitialAccessTokenResourceTest;
import org.keycloak.tests.admin.RoleByIdResourceTest;
import org.keycloak.tests.admin.UsersTest;
import org.keycloak.tests.admin.authentication.ExecutionTest;
import org.keycloak.tests.admin.authentication.InitialFlowsTest;
import org.keycloak.tests.admin.authentication.ProvidersTest;
import org.keycloak.tests.admin.authentication.RegistrationFlowTest;
import org.keycloak.tests.admin.authentication.ShiftExecutionTest;
import org.keycloak.tests.admin.client.ServiceAccountClientTest;

@Suite
@SelectClasses({
        AdminConsoleLandingPageTest.class,
        AttackDetectionResourceTest.class,
        CrossRealmPermissionsTest.class,
        InitialAccessTokenResourceTest.class,
        AdminHeadersTest.class,
        AdminPreflightTest.class,
        AttackDetectionResourceTest.class,
        ServiceAccountClientTest.class,
        UsersTest.class,
        ShiftExecutionTest.class,
        RegistrationFlowTest.class,
        ProvidersTest.class,
        InitialFlowsTest.class,
        ExecutionTest.class
})
public class ParallelSuite {
}
