FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean install -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Configuración de las variables de entorno con los datos proporcionados
ENV DATABASE_URL=r2dbc:postgresql://ep-aged-lake-a4wjkmtd-pooler.us-east-1.aws.neon.tech/neondb?sslmode=require \
    DATABASE_USERNAME=neondb_owner \
    DATABASE_PASSWORD=GQaLu2oDSTR6

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

# sdk install java 17.0.11-jbr
# mvn clean install -DskipTests
# mvn spring-boot:run
# docker build -t lizbet/prs1 .
# docker push lizbet/prs1
# docker-compose up
# kubectl port-forward service/prs1 8080:30001 -n 01-lizbet
