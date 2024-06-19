#!/bin/bash

# JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64" /opt/maven/apache-maven-3.5.4/bin/mvn clean package -DskipTests=true -e -P spring-boot

CURRENT_DIR=$(pwd)

cd ..

WORK_DIR=$(pwd)

echo "Setting up working dir: ${WORK_DIR}"

JAVA_HOME=/usr/lib/jvm/java-11 mvn clean package -DskipTests=true -e -P spring-boot || exit 1

# /usr/lib/jvm/openjdk-11-oracle/bin/java -Dmaven.multiModuleProjectDirectory=${WORK_DIR} -Dmaven.home=/opt/maven -Dclassworlds.conf=/opt/maven/bin/m2.conf -Dfile.encoding=UTF-8 -classpath /opt/maven/boot/plexus-classworlds-2.5.2.jar org.codehaus.classworlds.Launcher clean package -DskipTests=true -e -P spring-boot

echo ${JAVA_HOME};

rm -rfv "build/*.jar"

mkdir "build"

cp -Rv ./api/target/api-core.jar "build"

cd ${CURRENT_DIR}

exit;
