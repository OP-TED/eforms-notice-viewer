# eForms Notice Viewer

## Introduction

As input it takes an SDK view template EFX file and a notice XML file.
The output is HTML showing labels, values and so on.

## Building

Dependencies on other projects:
1. https://citnet.tech.ec.europa.eu/CITnet/stash/projects/TEDEFO/repos/eforms-sdk-lib/browse (git clone and `mvn clean install`)
2. ttps://citnet.tech.ec.europa.eu/CITnet/stash/projects/TEDEFO/repos/eforms-expression-language/browse (git clone and `mvn clean install`)
3. After the setup of dependencies `mvn clean install` in this project.

## Command line

Usage: <xml file to view> [<view id to use>]

Running from Maven example:

```
mvn compile exec:java -Dspring.datasource.username=${EFORMS_DATABASE_USERNAME} -Dspring.datasource.password=${EFORMS_DATABASE_PASSWORD} -Dexec.mainClass="eu.europa.ted.eforms.viewer.Application" -Dexec.args="arg1 agr2"
```

Example using notice sub type X02:

```
mvn compile exec:java -Dspring.datasource.username=${EFORMS_DATABASE_USERNAME} -Dspring.datasource.password=${EFORMS_DATABASE_PASSWORD} -Dexec.mainClass="eu.europa.ted.eforms.viewer.Application" -Dexec.args="X02_registration"
```