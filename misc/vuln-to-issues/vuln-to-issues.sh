#!/bin/bash -e

cd $(dirname $0)

ARGS="$@"

../../mvnw -q package exec:java -Dexec.mainClass=org.keycloak.misc.vulns.ScanDependenciesReport -Dexec.args="$ARGS"