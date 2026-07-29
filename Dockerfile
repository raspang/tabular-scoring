# Use a Maven image to build the application
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
# Copy the project files
COPY . .
# Build the application
RUN mvn clean package -Dvaadin.productionMode

# Use a JRE image to run the application
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
# Copy the JAR file from the build stage
COPY --from=build /app/target/tab-0.0.1-SNAPSHOT.jar .
# Expose the application port
EXPOSE 8080
# Run the application
ENTRYPOINT ["java", "-jar", "tab-0.0.1-SNAPSHOT.jar"]
