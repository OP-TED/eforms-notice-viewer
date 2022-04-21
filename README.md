# eForms Notice Viewer

## Introduction

As input it takes an SDK view template EFX file and a notice XML file.
The output is HTML showing labels, values and so on.

## Building

### Required technologies

* Java 11, higher may work for example Java 15 worked
* Maven 3.8.2, lower or higher may work

### Required other projects

1. https://citnet.tech.ec.europa.eu/CITnet/stash/projects/TEDEFO/repos/eforms-sdk-lib/browse (git clone and `mvn clean install`)
2. https://citnet.tech.ec.europa.eu/CITnet/stash/projects/TEDEFO/repos/eforms-expression-language/browse (git clone and `mvn clean install`)
3. After the setup of dependencies, run `mvn clean install` in this project from the same folder as the README.md

## Command line

Usage: `<xml file to view> <two letter language code> [<view id to use>]`

Example: I want to generate HTML for the file `X02_registration.xml` with `english` (en) labels.

Example running it using Maven:

```
mvn compile exec:java -Dspring.datasource.username=${EFORMS_DATABASE_USERNAME} -Dspring.datasource.password=${EFORMS_DATABASE_PASSWORD} -Dexec.mainClass="eu.europa.ted.eforms.viewer.Application" -Dexec.args="en X02_registration"
```

Read the logs in the console for details about the location of the generated XSL, HTML, ...
For technical details see Application.java for the entry point, also see unit tests
