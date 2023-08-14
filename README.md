# Mini(Key)cloak

## Build

```
./mvnw clean install -Dquarkus.package.type=native -DskipTests
```

## Initialise the database

```
podman run -d --name postgres --net host -e POSTGRES_DB=keycloak -e POSTGRES_USER=keycloak -e POSTGRES_PASSWORD=password postgres
```

Download Keycloak 21.1.1 and run the following:

```
bin/kc.sh start-dev --db postgres --db-url-host localhost --db-username keycloak --db-password password
```

Stop Keycloak 21.1.1



bin/kc.sh build --db postgres --cache local
bin/kc.sh start --optimized --hostname-strict false --http-enabled true --db-url-host localhost --db-username keycloak --db-password password


bin/kc.sh start-dev --hostname-strict false --http-enabled true --db postgres --db-url-host localhost --db-username keycloak --db-password password

kcw dev-build start  --db-url-host localhost --db-username keycloak --db-password password
