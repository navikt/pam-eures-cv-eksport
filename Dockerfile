FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:6cfc2c5e3791cc50d11fd4404844a276bec2dc970493db1118ff8170b7c00259

COPY build/libs/pam-eures-cv-eksport-*.jar /app.jar
EXPOSE 9030
ENV JAVA_TOOL_OPTIONS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

ENTRYPOINT ["java", "-jar", "/app.jar"]
