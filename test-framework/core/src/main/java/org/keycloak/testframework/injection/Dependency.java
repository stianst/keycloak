package org.keycloak.testframework.injection;

import org.jetbrains.annotations.NotNull;

public record Dependency(Class<?> valueType, String ref) {

    public Dependency {
        ref = StringUtil.convertEmptyToNull(ref);
    }

    @Override
    public @NotNull String toString() {
        return valueType.getSimpleName() + ":" + ref;
    }
}
