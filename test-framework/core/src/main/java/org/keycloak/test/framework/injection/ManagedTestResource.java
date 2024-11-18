package org.keycloak.test.framework.injection;

public abstract class ManagedTestResource {

    private boolean dirty = false;

    public abstract void runCleanupTasks();

    boolean isDirty() {
        return dirty;
    }

    public void dirty() {
        this.dirty = true;
    }

}
