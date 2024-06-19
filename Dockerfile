# mvn clean package -DskipTests=true -e -P spring-boot

FROM adoptopenjdk/openjdk11:jdk-11.0.14.1_1-alpine
MAINTAINER risk-q.com

RUN addgroup -S riskq \
  && adduser -S riskq -G riskq

RUN mkdir /opt/app \
  && chown riskq:riskq /opt/app

USER riskq:riskq

COPY api/target/api-core.jar /opt/app/api-core.jar

EXPOSE 80

ENTRYPOINT ["java","-jar","/opt/app/api-core.jar"]
