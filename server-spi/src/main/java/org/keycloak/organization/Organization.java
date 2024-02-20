package org.keycloak.organization;

import org.keycloak.models.UserModel;

import java.util.stream.Stream;

public interface Organization {

    String getName();
    void addUser(UserModel user);
    void removeUser(UserModel user);
    Stream<UserModel> listUsers();

}
