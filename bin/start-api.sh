#!/bin/bash

echo "Please run :\n"
echo "mvn clean package -P spring-boot\n"
echo "before start the server"

CURRENT_DIR=$(pwd)

cd ../api/target

export JAVA_HOME="/usr/lib/jvm/jdk-17.0.4"

echo $JAVA_HOME

echo "JAVA_OPTS=-Dspring.profiles.active=ekalosha,swagger" > api-core.conf

JAVA_HOME="/usr/lib/jvm/jdk-17.0.4" ./api-core.jar start

# JAVA_HOME="/usr/lib/jvm/jdk-17.0.4" ./api-core.jar start > __api-core.log &

cd ${CURRENT_DIR}
