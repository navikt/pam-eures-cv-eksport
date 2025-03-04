FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache bash
RUN adduser -u 1000 apprunner -D
USER apprunner

EXPOSE 8080

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
ENV JAVA_OPTS="-Xms768m -Xmx1280m"

COPY build/libs/pam-eures-cv-eksport-*.jar ./app.jar
CMD ["java", "-jar", "app.jar"]
