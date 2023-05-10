<#--
    Available variables:
    - translations: The available translations
    - body: The main content
    - templates: The available XSL templates
-->
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
  xmlns:ted="http://ted.europa.eu/efx" 
  exclude-result-prefixes="ted">

  <xsl:output method="html" encoding="UTF-8" indent="yes"/>
  <xsl:param name="language" />
  <xsl:variable name="labels" select="(${translations})"/>
  <xsl:decimal-format decimal-separator="${decimalSeparator}" grouping-separator="${groupingSeparator}" />
  
  <xsl:function name="ted:pluralSuffix" as="xs:string">
    <xsl:param name="n" as="xs:decimal"/>
    <xsl:choose>
      <xsl:when test="$n = 1 or $n = -1">
        <xsl:sequence select="''"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="'|plural'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

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