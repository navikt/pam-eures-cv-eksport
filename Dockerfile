FROM navikt/java:11

ENV JAVA_OPTS="-Xms768m -Xmx1280m"

COPY scripts/init_secrets.sh /init-scripts/init_secrets.sh
COPY build/libs/pam-eures-cv-eksport-*-all.jar ./app.jar