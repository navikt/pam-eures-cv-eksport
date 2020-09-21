
### Oppsett på Mac
`brew install postresql`

Og start med `postgres -D /usr/local/var/postgres`.

Opprett databasen `pam-eures-cv-eksport` med tilhørende bruker `pam-eures-cv-eksport-admin` i din favoritt PSQL klient 
på localhost.

### For å kjøre appen lokalt

`RUN_LOCAL=true gradle run`

RUN_LOCAL plukkes opp i `gradle.build` og sender med `-Dmicronaut.environments=local` som parameter til java.
Dette plukkes igjen opp i  `DataSourceFactory.kt` og disabler Hikari og Vault integrasjonen.


### Kafka

Ved lokal kjøring kobler appen til env variabelen KAFKA_HOST på port 9092 (`application-local.yml`). Her kan man bruke 
`pam-cv-api` sin Kafka VM og bruke nevnte app sine syntetiske test data for å ha noen meldinger å konsumere. Sjekk 
readme i CV API for instruksjoner.

### XML test mapping
I `Konverterer.kt` finnes det en `testing()` funksjon (kjøres automagisk) som dumper XML for en hardkodet syntetisk 
aktør til en fil ved navn cv_UUID.xml

### XML Validator
https://webgate.acceptance.ec.europa.eu/eures/eures-tools/debug-tool/page/main#/validator-tool
(Brukernavn og passord i vault for team-pam)