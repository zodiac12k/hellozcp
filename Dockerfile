FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/hellozcp-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENV AWS_SECRET_KEY="1234q38rujfkasdfgws"
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar