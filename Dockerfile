FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:7fb16eed0a7b7212c2c37bafb52a207978def857e32137a183df27a01dc6a64d

COPY build/libs/pam-eures-cv-eksport-*.jar /app/app.jar

ENV JDK_JAVA_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb_NO.UTF-8' TZ="Europe/Oslo"

EXPOSE 9030

CMD ["-jar", "/app/app.jar"]
