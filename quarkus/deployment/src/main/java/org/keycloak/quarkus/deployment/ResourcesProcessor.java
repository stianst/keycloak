package org.keycloak.quarkus.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;

public class ResourcesProcessor {

    @BuildStep
    NativeImageResourcePatternsBuildItem nativeImageResourceBuildItem() {
        return NativeImageResourcePatternsBuildItem.builder()
                .includePatterns("theme/.*")
                .includePatterns("org/keycloak/protocol/oidc/endpoints/.*.html")
                .build();
    }

}
