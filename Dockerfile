FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:e5c0b3ba352f0850a786961b083c44a1134ceb59c8e12db9deaf077fd02c28dc

COPY build/libs/pam-eures-cv-eksport-*.jar /app.jar
EXPOSE 9030
ENV JAVA_TOOL_OPTS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

ENTRYPOINT ["java", "-jar", "/app.jar"]
