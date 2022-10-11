# eForms Notice Viewer

> Copyright 2022 European Union
>
>Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission –
subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at: 
https://joinup.ec.europa.eu/software/page/eupl5
>
>Unless required by applicable law or agreed to in writing, software distributed under the Licence is
distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under 
the Licence.

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

1. Unzip the binary distribution (a .zip file), e.g. to `/path/to/eforms-notice-viewer`.

   The unpacked folder will contain the following:
     - A JAR file named eforms-notice-viewer-<version>.jar
     - A `eforms-sdk` containing all of the supported eForms SDKs

2. Go to `/path/to/eforms-notice-viewer` and execute the following:

   ```
   java -jar eforms-notice-viewer-<version>.jar <language> <xml file> [<view id>] [<SDK resources>]
   ```

   - `<language>`: two letter code for the desired language of the output (en, fr, etc.)
   - `<xml file>`: path to the XML notice to view
   - `<view id>`: identifier of the view template to use. If omitted, the default template for the notice subtype indicated in the XML notice is used.
   - `<SDK resources>`: path to the folder containing the SDK resources, organized by version. If omitted, the folder `eforms-sdk` is used.

### Example

To generate an HTML to view the file `X02_registration.xml` in `en` (English):

```
java -jar eforms-notice-viewer-<version>.jar en eforms-sdk/0.6/examples/notices/X02_registration.xml
```

To generate an HTML for the summary view of the file `cn_24_minimal.xml` in `en` (English):

```
java -jar eforms-notice-viewer-<version>.jar en eforms-sdk/0.6/examples/notices/cn_24_minimal.xml summary
```

While running, log output is generated in the console, giving information on the location of the generated XSL, HTML, and any problem encountered.




