# Mini(Key)cloak

## Initialise the database

The native distribution doesn't currently contain Liquibase (which is kinda on purpose as we want to see how small and fast
we can make it), so you'll need to use Keycloak 21.1.1 to initialise the database first.

```
podman run -d --name postgres --net host -e POSTGRES_DB=keycloak -e POSTGRES_USER=$USER -e POSTGRES_PASSWORD=password postgres
```

Download Keycloak 21.1.1 and run the following:

```
bin/kc.sh start-dev --db postgres --db-url-host localhost --db-username $USER --db-password password
```

Stop Keycloak 21.1.1!

## Build

```
./native-build.sh
```

Should take around 5 min on a relatively fast box. If you have some issues building check out https://quarkus.io/guides/building-native-image

## Start Keycloak in native mode

```
./native-start.sh
```

Starts fast, with a bunch of things removed (Liquibase, Infinispan, SAML, etc.), and only some stuff works like:

* Welcome page ;)
* Login to admin console, but admin console itself doesn't work
* well-known endpoint

Other things may or may not work, who knows as there's been no tests ran against it ;)