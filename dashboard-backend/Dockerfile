FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY shared-contracts/pom.xml shared-contracts/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY match-generator/pom.xml match-generator/pom.xml
COPY betting-api/pom.xml betting-api/pom.xml
COPY match-simulator/pom.xml match-simulator/pom.xml
COPY results-engine/pom.xml results-engine/pom.xml
COPY dashboard-backend/pom.xml dashboard-backend/pom.xml
COPY email-worker/pom.xml email-worker/pom.xml
RUN mvn -B -pl dashboard-backend -am dependency:go-offline
COPY shared-contracts/src shared-contracts/src
COPY dashboard-backend/src dashboard-backend/src
RUN mvn -B -pl dashboard-backend -am clean package -DskipTests && cp dashboard-backend/target/dashboard-backend-1.0.0.jar /app.jar
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=build --chown=appuser:appgroup /app.jar app.jar
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
