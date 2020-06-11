FROM navikt/java:11
COPY scripts/init_secrets.sh /init-scripts/init_secrets.sh
COPY build/libs/pam-eures-cv-eksport-*-all.jar ./app.jar