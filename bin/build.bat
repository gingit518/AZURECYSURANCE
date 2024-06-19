@ECHO OFF

echo "Starting API build"
call cd ..

set JAVA_HOME=c:\\Program Files\\Java\\jdk-11.0.11

echo JAVA_HOME is set to %JAVA_HOME%
call mvn clean package -P spring-boot -e -DskipTests=true

call rm -rfv "build/*.jar"
call mkdir "build"
call cp -Rv ./api/target/api-core.jar "build"
call cd bin
