# eForms Notice Viewer

## Introduction

As input it takes an SDK view template EFX file and a notice XML file.
The output is HTML showing labels, values and so on.


## Building

### Required technologies

* Java 11, higher may work for example Java 15 works
* Maven 3.8.2 as a reference but lower or higher may work

### Required projects

You will need https://citnet.tech.ec.europa.eu/CITnet/stash/projects/TEDEFO/repos/eforms-expression-language/browse (git clone, see README of that project)
After the setup, run `mvn clean install` in this project from the same folder as the `pom.xml`


## Command line

Usage: `<two letter language code> <xml file to view (without .xml)> [<view id to use>]`

### Example

I want to generate an HTML to view the file `X02_registration.xml` in `en` (english). Running it using Maven:

```
mvn compile exec:java -Dspring.datasource.username=${EFORMS_DATABASE_USERNAME} -Dspring.datasource.password=${EFORMS_DATABASE_PASSWORD} -Dexec.mainClass="eu.europa.ted.eforms.viewer.Application" -Dexec.args="en X02_registration"
```

After running it read the logs in the console for details about the location of the generated XSL, HTML, ...
For technical details see `Application.java` for the entry point, also see unit tests for usage.
