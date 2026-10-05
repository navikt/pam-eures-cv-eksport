FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:9f124e43e7d3c8605f42c6078f898421c9386087d87f1e1829a5bdc4e2a56bea

COPY build/libs/pam-eures-cv-eksport-*.jar /app.jar
EXPOSE 9030
ENV JAVA_TOOL_OPTIONS="-XX:-OmitStackTraceInFastThrow -Xms256m -Xmx1536m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

ENTRYPOINT ["java", "-jar", "/app.jar"]
