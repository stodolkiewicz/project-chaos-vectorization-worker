### STAGE 1: BUILDER ###
FROM mirror.gcr.io/library/eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline
COPY ./src ./src

RUN ./mvnw clean package -DskipTests

### STAGE 2: FINAL ###
FROM mirror.gcr.io/library/eclipse-temurin:21-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080

COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar

ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
# https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/

