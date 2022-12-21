# eForms Notice Viewer


_Copyright 2022 European Union_

_Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission –
subsequent versions of the EUPL (the "Licence");_
_You may not use this work except in compliance with the Licence._
_You may obtain a copy of the Licence at:_ 
_https://joinup.ec.europa.eu/software/page/eupl5_

_Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence._

---

## Introduction

This is a sample command line application which demonstrates how you can use the eForms SDK in a metadata driven application that visualises eForms notices.

As input it takes a notice XML file, a language code, and optionally an EFX template..
The output is an HTML page showing the content of the notice, with labels in the desired language, values and so on.

The notice viewer is not a production ready application. It is intended to demonstrate the use of concepts and resources available in the eForms SDK. You can use it as an inspiration or a starting point for your own apps.

The documentation is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer

## Building

Requirements:

* Java 11 or higher
* Maven 3.8, other versions probably also work

This project depends on both the eForms SDK and the EFX toolkit for Java, and uses their respective Maven packages.

Execute the following on the root folder of this project:

```
mvn clean install
```

For forcing update of snapshots:

```
mvn clean install -U
```

## Usage

### Requirements

* Java 11 or higher

### Command line

Execute the binary `eforms-notice-viewer-<version>-app.jar` as follows:

    java -jar eforms-notice-viewer-<version>-app.jar [OPTIONS] <language> <xml file>

The available OPTIONS are:

- `-f` or `--force`: Forces re-building of XSL by clearing any cached content.
- `-h` or `--help`: Shows a help message and exits.
- `-i` or `--viewId`: Identifier of the view template to use. If omitted, the default template for the notice subtype indicated in the XML notice is used.
- `-p` or `--profileXslt`: Enables XSLT profiling.
- `-r` or `--sdkRoot`: SDK resources root folder.
- `-t` or `--templatesRoot`: Templates root folder.
- `-V` or `--version`: Prints version information and exits.

The required parameters are:

- `<language>`: two letter code for the desired language of the output (en, fr, etc.)
- `<xml file>`: path to the XML notice to view

### Example

To generate an HTML to view the file `X02_registration.xml` in `en` (English):

```
java -jar eforms-notice-viewer-<version>-app.jar en eforms-sdk/1/0/examples/notices/X02_registration.xml
```

To generate an HTML for the summary view of the file `cn_24_minimal.xml` in `en` (English):

```
java -jar eforms-notice-viewer-<version>-app.jar -i summary en eforms-sdk/1.0/examples/notices/cn_24_minimal.xml
```

While running, log output is generated in the console as well as under a folder named `logs`, giving information on the location of the generated XSL, HTML, and any problem encountered.

### Templates for markup generation

The generated XSL markup is compiled using Freemarker templates.

These templates are bundled with the distributed JAR file and extracted to a folder named "templates" under the same folder where the application is started from.

The extracted templates under this folder can be altered in order to customize the generated markup.

**NOTE 1:** The folder's path can be changed by using the option "-t".
**NOTE 2:** Any change to the templates will require the usage of the option "-f", which removes the cached XSL and causes it to be rebuilt.
