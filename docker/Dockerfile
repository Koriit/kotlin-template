FROM openjdk:8-jdk-stretch

ARG APP_VERSION
ADD build/libs/my-app-${APP_VERSION}.jar /application.jar

ENV APP_OPTS="$APP_OPTS"

EXPOSE 8080

ENTRYPOINT java $JAVA_OPTS -jar application.jar $APP_OPTS
