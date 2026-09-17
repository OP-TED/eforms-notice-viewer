# eForms Notice Viewer 0.12.0 - Release Notes

_The eForms Notice Viewer is a sample command line application that demonstrates how you can use the [eForms SDK](https://github.com/OP-TED/eForms-SDK) in a metadata driven application that visualises eForms notices._

---

## In this release

### JSON output

- Added a JSON output generator alongside the existing HTML output. The JSON output provides the body, summary, and navigation sections as separate fields, allowing applications to render and place them independently in their UI.

### XSL markup improvements

- Implemented rendering of all new EFX-2 template features (callable templates, global variables and functions, dictionaries, hyperlinks, summary and navigation sections).
- Freemarker templates have been reorganised from `xsl_markup/` into `xsl-markup/html/` and `xsl-markup/json/` subfolders.

### Profiling

- Restructured profiling into a configuration enum (`ProfilerConfig`) supporting both XSLT and EFX translation profiling, replacing the previous boolean flag.

### CLI improvements

- Added an option to prefer snapshot SDK versions.

### Moved to core library

- `NoticeDocument` and `SafeDocumentBuilder` have been moved to the eForms Core Library. The viewer now uses the core library versions.

### Dependencies

- Updated to EFX Toolkit 2.0.0-alpha.6 and eForms Core Library 1.6.0.

---

Documentation for this sample application is available at: https://docs.ted.europa.eu/eforms/latest/notice-viewer

This version depends on:

- [EFX toolkit for Java](https://github.com/OP-TED/efx-toolkit-java) version 2.0.0-alpha.6.
- [eForms Core for Java](https://github.com/OP-TED/eforms-core-java) library version 1.6.0.
