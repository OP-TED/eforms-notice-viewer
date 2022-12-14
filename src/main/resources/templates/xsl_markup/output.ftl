<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
    xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
    xmlns:efext="http://data.europa.eu/p27/eforms-ubl-extensions/1"
    xmlns:efac="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1"
    xmlns:efbc="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1"
    xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
>
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>
    <xsl:param name="language" />
    <xsl:variable name="labels" select="(${translations})"/>
    <xsl:template match="/">
        <html>
            <head>
                <style>
                    section { padding: 6px 6px 6px 36px; }
                    .text { font-size: 12pt; color: black; }
                    .label { font-size: 12pt; color: green; }
                    .dynamic-label { font-size: 12pt; color: blue; }
                    .value { font-size: 12pt; color: red; }
                </style>
            </head>
            <body>
                <#list body as markup>
                    ${markup}
                </#list>
            </body>
        </html>
    </xsl:template>
    
    <#list templates as template>
        ${template}
    </#list>
</xsl:stylesheet>
