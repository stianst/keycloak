package org.keycloak.quarkus.runtime;

import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;

public class DummyTimerProvider implements TimerProvider {
    @Override
    public void schedule(Runnable runnable, long intervalMillis, String taskName) {

    }

    @Override
    public void scheduleTask(ScheduledTask scheduledTask, long intervalMillis, String taskName) {

    }

    @Override
    public TimerTaskContext cancelTask(String taskName) {
        return null;
    }

    @Override
    public void close() {

    }
}
