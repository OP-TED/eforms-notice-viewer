# eForms Notice Viewer

## Introduction

This is a sample command line application which demonstrates the usage of the SDK views and EFX.
As input it takes an SDK view template EFX file and a notice XML file.
The output is HTML showing labels, values and so on.

Being a sample application, the HTML generation has been simplified for demonstration purposes.

## Building

### Requirements

* Java 11 or higher
* Maven 3.8.2 as a reference, but lower or higher may work

### Required projects

Execute the following on the root folder of this project:

```
mvn clean install
```

## Execution

### Requirements

* Java 11 or higher

### Command line

1. Unzip the binary distribution (a .zip file), e.g. to `/path/to/eforms-notice-viewer`.

   The unpacked folder will contain the following:
     - A JAR file named eforms-notice-viewer-<version>.jar
     - A `eforms-sdk` containing all of the supported eForms SDKs

2. Go to `/path/to/eforms-notice-viewer` and execute the following:

   ```
   java -jar eforms-notice-viewer-<version>.jar <options>
   ```

   where `<options>` is: `<two letter language code> <path of XML file to view> [<view id to use>] [SDK resources root folder]`

### Example

To generate an HTML to view the file `X02_registration.xml` in `en` (english), execute::

```
java -jar eforms-notice-viewer-<version>.jar en eforms-sdk/0.6/examples/notices/X02_registration.xml
```

After running it read the logs in the console for details about the location of the generated XSL, HTML, ...
For technical details see `Application.java` for the entry point, also see unit tests for usage.
