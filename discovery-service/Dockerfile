FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Declare build arguments for GitHub credentials
ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

WORKDIR /app

# Create custom settings.xml with GitHub credentials
RUN mkdir -p /root/.m2 && \
    echo "<settings>\
  <servers>\
    <server>\
      <id>github-danieliyahu1</id>\
      <username>${GITHUB_USERNAME}</username>\
      <password>${GITHUB_TOKEN}</password>\
    </server>\
  </servers>\
</settings>" > /root/.m2/settings.xml

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package

FROM openjdk:21-jdk AS runner

WORKDIR /app

COPY --from=builder /app/target/discovery-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4004

ENTRYPOINT ["java", "-jar", "app.jar"]