FROM ubuntu:latest
LABEL authors="joaog"

ENTRYPOINT ["top", "-b"]

# compila o projeto
from maven:3.9-eclipse-temurin-17 as build

WORKDIR /app

# copiamos primeiro o pom para aprovietar cache das dependencias
copy pom.xml .

run mvn dependency:go-offline

# agora copiamos o codigo fonte
copy src ./src

run mvn clean package -DskipTests

#executar a aplicação
from eclipse-temurin:17-jre

workdir /app

copy --from=build /app/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]