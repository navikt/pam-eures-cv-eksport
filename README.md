# pam-eures-cv-eksport

Implementation of the Eures input API, used by EU and EURES to fetch CV´s in Norway.

### Krav for utviklere

Du trenger Docker installert lokalt, i tillegg til et "vanlig" oppsett med Java og Maven.

Du må installere [docker-compose](https://docs.docker.com/compose/install/#install-compose)

Du kan så teste innlesing av CV´er via kafka ved å kjøre pam-cv-api lokalt med kafka i bakgrunnen (via docker-compose).
Se Readme i pam-cv-api for dokumentasjon rundt dette. 

Når lokal kafka og pam-cv-api kjører kan du starte pam-eures-cv-eksport med følgende vm-options: 

-Dspring.profiles.active=dev -DKAFKA_HOST_IP=<OUTPUT_FRA_DOCKER_MACHINE_IP (mac)>/<localhost(win/linux)>  -DKAFKA_BROKER_PORT=9092 -DSCHEMA_REGISTRY_PORT=8081 -DZK_PORT=2181  
