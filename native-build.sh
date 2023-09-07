#!/bin/bash -e

POM=pom.xml
if [ "$(basename $PWD)" == "quarkus" ]; then
  cd ..
  POM=quarkus/pom.xml
fi

#./mvnw clean install -DskipTests -DskipTestsuite -Dmaven.build.cache.enabled=true -Dquarkus.package.type=native -Dquarkus.native.debug.enabled -Dquarkus.native.additional-build-args="--initialize-at-run-time=org.apache.xml.security.stax.ext.XMLSecurityConstants,--enable-url-protocols=jar,-H:-DeleteLocalSymbols"

./mvnw clean install -DskipTests -DskipTestsuite -Dmaven.build.cache.enabled=true -Dquarkus.package.type=native -Dquarkus.native.additional-build-args="--initialize-at-run-time=org.apache.xml.security.stax.ext.XMLSecurityConstants,--enable-url-protocols=jar"
