FROM navikt/java:18

ENV JAVA_OPTS="-Xms768m -Xmx1280m"

COPY build/libs/pam-eures-cv-eksport-*.jar ./app.jar
