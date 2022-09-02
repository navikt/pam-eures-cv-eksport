FROM navikt/java:17

ENV JAVA_OPTS="-Xms768m -Xmx1280m"

COPY build/libs/pam-eures-cv-eksport-*.jar ./app.jar
