FROM openjdk:17-jdk-slim

WORKDIR /app

ARG todogrpc=target/grpc-todo-app-3.2.6.jar

COPY ${todogrpc} app/todogrpc.jar

EXPOSE 9009

ENTRYPOINT [ "java", "-jar", "app/todogrpc.jar" ]