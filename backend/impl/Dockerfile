# Use official Maven image to build our Spring Boot application
FROM maven:3.9.3-amazoncorretto-20 AS build

# Set the working directory
WORKDIR /usr/src/app

# Copy the pom.xml and source code to the container
COPY ./pom.xml ./
COPY ./src ./src/

# Package the application
RUN mvn package -DskipTests

# Use OpenJDK to run our Java application
FROM openjdk:20-slim

# Copy the jar file from the build stage
COPY --from=build /usr/src/app/target/*.jar app.jar

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]