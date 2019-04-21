#!/usr/bin/env bash

mvn clean package
mvn install:install-file \
-Dfile=target/DAWG-1.0.jar \
-DgroupId=org.quinto.dawg \
-DartifactId=DAWG \
-Dversion=1.0 \
-Dpackaging=jar
