#!/bin/bash

./mvnw clean install -Dquarkus.package.type=native -DskipTests -Dquarkus.native.additional-build-args="--enable-url-protocols=jar"