FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:9eec756379ed5f12ebade505e5b7ecc26e2eb55b46c137c0c47aad8a1757913c

COPY build/libs/pam-eures-cv-eksport-*.jar /app/app.jar

ENV JDK_JAVA_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

EXPOSE 9030

CMD ["-jar", "/app/app.jar"]
