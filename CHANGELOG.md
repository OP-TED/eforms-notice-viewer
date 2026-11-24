# eForms Notice Viewer 0.14.0 - Release Notes

_The eForms Notice Viewer is a sample command line application that demonstrates how you can use the [eForms SDK](https://github.com/OP-TED/eForms-SDK) in a metadata driven application that visualises eForms notices._

---

## In this release

This release raises the minimum Java version to 21.

### Java 21

- The minimum supported Java version is now 21 (previously 11). Both building and running the application require a Java 21 or later JDK/JRE.
- The GitHub Actions workflows now build, check the javadoc, and publish using Temurin 21. The retired `adopt` distribution has been replaced by `temurin`, which provides Java 21 builds.

---

Documentation for this sample application is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer

---

# eForms Notice Viewer 0.13.0 - Release Notes

_The eForms Notice Viewer is a sample command line application that demonstrates how you can use the [eForms SDK](https://github.com/OP-TED/eForms-SDK) in a metadata driven application that visualises eForms notices._

---

## In this release

This release updates the Viewer to the latest released EFX Toolkit and eForms Core Library versions and corrects the CLI documentation.

### CLI documentation

- The README and CLI help now accurately document `-p` / `--profile`. An explicit value is required: `xslt`, `efx`, or `all`. Values can be comma-separated, such as `--profile=xslt,efx`; `--profile=all` enables both profilers.
- The README now documents `-s` / `--snapshots`, which allows downloading snapshot SDK versions.

---

Documentation for this sample application is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer

This version depends on:

- [EFX Toolkit for Java](https://github.com/OP-TED/efx-toolkit-java/releases/tag/2.0.0-alpha.9) version 2.0.0-alpha.9.
- [eForms Core for Java](https://github.com/OP-TED/eforms-core-java/releases/tag/1.9.1) library version 1.9.1.
