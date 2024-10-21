## Beskrivelse 
`pam-eures-cv-eksport` er et API som tilrettelegger for innhenting av CV-data fra [EURES](https://ec.europa.eu/eures/public/homepage). Den tar seg av lagring av samtykke for deling av CV-data med EURES (gjennom [pam-personbruker](https://github.com/navikt/pam-personbruker)), samt konvertering av dataene til riktig format.

## Api-dokumentasjon
Swagger-ui av endepunktene kan finnes på https://arbeidsplassen.intern.dev.nav.no/pam-eures-cv-eksport/swagger-ui.html
Den kan også finnes på http://localhost:9030/pam-eures-cv-eksport/swagger-ui.html hvis applikasjonen kjører lokalt.
For å få den til å enable dokumentasjonen lokalt, må `SWAGGER_ENABLED=true` legges til som en miljøvariabel.

## For å kjøre appen lokalt

Postgres med applikasjonens database, pam-eures-cv-eksport, kafka oppsett og mock-oauth2-server må kjøre lokalt.
Dette kan startes med scriptet under

```shell 
./start-docker-compose.sh
 ```

Legg inn innslag i /etc/hosts med `127.0.0.1 host.docker.internal` hvis dette mangler.

### Kommandolinje
`gradle run --args='--spring.profiles.active=dev'`

Dette kjører opp appen vha gradle. Da puttes også src/test/* på classpath. Der finnes det en logback-test.xml som gjør
logging litt hyggeligere. Det må kjøres med en annen profil enn test, siden det som ligger i test-properties kun er for testene og ikke lokal kjøring.  

### Intellij
Bruk IntelliJ sin gradle-plugin, og velg Tasks -> application -> run. Da får man de
nødvendige ting på classpathen

## Oppsett av database
Databasen kan opprettes og startes med å kjøre: 
`./start-docker-compose.sh`
Dette oppretter en database som heter pam-eures-cv-eksport, med brukernavn `postgres` og default passord. 

Hvis du har behov for å koble på databasen å sjekke ting manuelt, så kan det også gjøres i favorittklienten din
* Eksempel med psql `psql --user=postgres -d pam-eures-cv-eksport`

## XML test mapping
I `Konverterer.kt` finnes det en `testing()` funksjon (kjøres automagisk) som dumper XML for en hardkodet syntetisk 
aktør til en fil ved navn cv_UUID.xml

## XML Validator
https://webgate.acceptance.ec.europa.eu/eures/eures-tools/debug-tool/page/main#/validator-tool
(Brukernavn og passord i vault for team-pam)

## Avhengigheter
  - Kafka topic: arbeid-pam-cv-endret-v6
