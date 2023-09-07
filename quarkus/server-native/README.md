# Building

Full build:
```
mvn clean install -Dnative
```

Or, after building the full project to only build the native dist:
```
mvn -f quarkus/server-native clean install
```

# Starting

```
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=admin
quarkus/server/target/lib/keycloak-runner start-dev
```

# Stopping

There's some issue causing it not to stop cleanly, so needs to be forcefully shutdown:

```
killall -9 keycloak-runner
```

# Benchmarking

http://localhost:8080/realms/master/benchmark
