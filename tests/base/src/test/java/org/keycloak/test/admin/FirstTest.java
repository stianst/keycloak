package org.keycloak.test.admin;

import org.junit.jupiter.api.Test;
import org.keycloak.test.framework.annotations.InjectRealm;
import org.keycloak.test.framework.annotations.KeycloakIntegrationTest;
import org.keycloak.test.framework.realm.ManagedRealm;

@KeycloakIntegrationTest
public class FirstTest {

    @InjectRealm
    ManagedRealm realm;

    @Test
    public void testOne() throws InterruptedException {
        System.out.println("Start " + Thread.currentThread().getName());
        System.out.println(realm.getName() + Thread.currentThread().getName());
        Thread.sleep(10000);
        System.out.println("Finish " + Thread.currentThread().getName());
    }

    @Test
    public void testTwo() throws InterruptedException {
        System.out.println("Start " + Thread.currentThread().getName());
        System.out.println(realm.getName() + Thread.currentThread().getName());
        Thread.sleep(10000);
        System.out.println("Finish " + Thread.currentThread().getName());
    }

    @Test
    public void testThree() throws InterruptedException {
        System.out.println("Start " + Thread.currentThread().getName());
        System.out.println(realm.getName() + Thread.currentThread().getName());
        Thread.sleep(10000);
        System.out.println("Finish " + Thread.currentThread().getName());
    }

    @Test
    public void testFour() throws InterruptedException {
        System.out.println("Start " + Thread.currentThread().getName());
        System.out.println(realm.getName() + Thread.currentThread().getName());
        Thread.sleep(10000);
        System.out.println("Finish " + Thread.currentThread().getName());
    }

}
