# eForms Notice Viewer

The eForms Notice Viewer is a command-line application that uses the eForms SDK to visualize eForms notices. This sample application takes an eForms notice XML file, a language code, and optionally an EFX template as input. It then generates an HTML page that displays the content of the notice, with labels in the desired language, values, and other relevant information.

The eForms Notice Viewer is intended to demonstrate the use of concepts and resources available in the eForms SDK. You can use it as an inspiration or a starting point for your own applications.

> <sub>**_Copyright 2022 European Union_**<br/>
*Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission – subsequent versions of the EUPL (the "Licence"); You may not use this work except in compliance with the Licence. You may obtain a copy of the Licence at: https://joinup.ec.europa.eu/software/page/eupl*<br/>
*Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licence for the specific language governing permissions and limitations under the Licence.*<br/></sub>

## Overview

In this README, you will find information on how to build and use the eForms Notice Viewer, as well as how to contribute to the project. The documentation for the eForms Notice Viewer is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer.

## Building

To build the eForms Notice Viewer, you will need the following:

* Java 11 or higher
* Apache Maven 3.8 or later (other versions probably work, but they have not been tested)


This project depends on both the eForms SDK and the EFX toolkit for Java, and uses their respective Maven packages. You do not need to manually install these dependencies, as Maven will download and install them automatically during the build process.

To build the application, open a terminal window and navigate to the root folder of this project. Then, run the following command:


```
mvn clean install
```

This command will compile the source code, run the tests, and package the application into a JAR file. The JAR file will be located in the target folder.

If you need to force an update of any snapshots, you can add the -U flag to the command:

```
mvn clean install -U
```

This will force Maven to update any snapshots that are used as dependencies in the project.

In order to be able to use \'xxx-SNAPSHOT\'[^1] versions of dependencies you will need to add the following repository in the **\<repositories\>** section in your settings.xml file:

[^1]: Release versions of the dependencies are provided by default in the maven central repo
```
<repository>
  <id>oss-snapshots</id>
  <name>OSS Snapshots repository</name>
  <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

## Usage    

### Requirements

Before using this application, please make sure that you have the following requirements installed:

* Java 11 or higher

### Command line

To use the application, execute the binary eforms-notice-viewer-<version>-app.jar in the command line using the following syntax:

```
java -jar eforms-notice-viewer-<version>-app.jar [OPTIONS] <language> <xmlFile>
```

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
- `<xmlFile>`: path to the XML notice to view

### Examples

Here are some examples of how to use the application:

* To generate an HTML to view the file `X02_registration.xml` in `en` (English):
    ```
    java -jar eforms-notice-viewer-<version>-app.jar en eforms-sdk/1/0/examples/notices/X02_registration.xml
    ```

* To generate an HTML for the summary view of the file `cn_24_minimal.xml` in `en` (English):
    ```
    java -jar eforms-notice-viewer-<version>-app.jar -i summary en eforms-sdk/1.0/examples/notices/cn_24_minimal.xml
    ```

While running, log output is generated in the console as well as under a folder named `logs`, giving information on the location of the generated XSL, HTML, and any problem encountered.

### Customizing the generated markup

The generated XSL markup is compiled from template fragments stored as Freemarker templates, which are bundled with the distributed JAR file. These templates can be found in the `templates` folder located in the same directory as the application.

To customize the generated markup, modify the templates in the `templates` folder. It's important to note that any changes made to the templates will require the cached XSL to be rebuilt. You can do this by using the `-f` option, which removes the cached XSL and causes it to be rebuilt.

If you want to use a different folder for the templates, you can specify the path using the `-t` option followed by the desired folder path. This allows for greater flexibility in experimenting with the generated markup.

## Contributing

We welcome contributions to the eForms Notice Viewer! If you'd like to contribute, please follow these steps:

1. Fork the repository and create a new branch for your contribution.
0. Make your changes and ensure that all tests pass.
0. Submit a pull request with a clear description of your changes and the problem they solve.
0. We'll review your pull request and provide feedback.

Please note that by contributing to this project, you agree to license your contributions under the terms of the EUPL, Version 1.2 or subsequent versions approved by the European Commission.