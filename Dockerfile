FROM ubuntu
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} svc-notification-admin.jar
ENTRYPOINT ["java","-jar","/svc-notification-admin.jar"]
