## Beskrivelse 
`pam-eures-cv-eksport` er et API som tilrettelegger for innhenting av CV-data fra [EURES](https://ec.europa.eu/eures/public/homepage). Den tar seg av lagring av samtykke for deling av CV-data med EURES (gjennom [pam-personbruker](https://github.com/navikt/pam-personbruker)), samt konvertering av dataene til riktig format. 

## For å kjøre appen lokalt

Det kreves at enkelte ting kjører på PC
1. Postgres med applikasjonens database, pam-eures-cv-eksport
1. Kafka-oppsett fra CV. Se nedenfor 

### Kommandolinje
`gradle run`

Dette kjører opp appen vha gradle. Da puttes også src/test/* på classpath. Der finnes det en logback-test.xml som gjør
logging litt hyggeligere. I tillegg finnes no.nav.security:token-validation-test-support på test-class-path, slik at man
kan teste endepunktene også 

### Intellij
Bruk IntelliJ sin gradle-plugin, og velg Tasks -> application -> run. Da får man de
nødvendige ting (no.nav.security:token-validation-test-support) på classpathen

## Oppsett av database første gang
Opprett database med applikasjonens defaultbruker 

STart med å installere og starte postgresserveren - hvis det ikke allerede er på plass
* Installere: `brew install postgresql`
* Start postgres-server og la den gå. For eksempel ved `postgres -D /usr/local/var/postgres`

Opprett bruker `pam-eures-cv-eksport` uten passord, og database `pam-eures-cv-eksport` som eies av denne brukeren i din 
favorittklient.
Eksempler ved postgres' kommandolinje-verktøy
* `createuser -e pam-eures-cv-eksport`
* `createdb --owner=pam-eures-cv-eksport pam-eures-cv-eksport`.

### Oppstart senere
Sørg for at samme postgres kjører `postgres -D /usr/local/var/postgres`.

Hvis du har behov for å koble på databasen å sjekke ting manuelt, så kan det også gjøres i favorittklienten din
* Eksempel med psql `psql --user=pam-eures-cv-eksport -d pam-eures-cv-eksport`

## Kafka
Kan starte dockerimage med pam-cv-api's kafka-topic vha `pam-cv-api/migrering/src/test/resources/cv-pipeline.sh up`
Den starter kafka med broker på `localhost:9091`, og schema registry på `localhost:8081` Dette
er defaultverdier i appen

Her kan man bruke  `pam-cv-api` sin Kafka VM og bruke nevnte app sine syntetiske test data for å ha noen meldinger 
å konsumere. Sjekk README i CV API for instruksjoner.

## XML test mapping
I `Konverterer.kt` finnes det en `testing()` funksjon (kjøres automagisk) som dumper XML for en hardkodet syntetisk 
aktør til en fil ved navn cv_UUID.xml

## XML Validator
https://webgate.acceptance.ec.europa.eu/eures/eures-tools/debug-tool/page/main#/validator-tool
(Brukernavn og passord i vault for team-pam)

## Avhengigheter
  - Kafka topic: arbeid-pam-cv-endret-v6
