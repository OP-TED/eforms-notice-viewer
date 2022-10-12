# eForms Notice Viewer 0.5.0 Release Notes

_The eForms Notice Viewer is a sample command line application that demonstrates how you can use the [eForms SDK](https://github.com/OP-TED/eForms-SDK) in a metadata driven application that visualises eForms notices._

---
## In this release:

In this release we moved some utility classes to a new java library: [eforms-core-java](https://github.com/OP-TED/eforms-core-java). These utility classes allowed the automatic discovery and download of new versions of the eFroms SDK. We decided to extract this functionality to a new shared library so that it can also be used by other applications.  

--- 

Documentation for this sample application is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer

This version depends on:
 - [EFX toolkit for Java](https://github.com/OP-TED/efx-toolkit-java) version 1.1.0.
 - [eForms Core Java](https://github.com/OP-TED/eforms-core-java) library version 0.1.0.
