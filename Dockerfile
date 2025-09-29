FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests
RUN cp target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]