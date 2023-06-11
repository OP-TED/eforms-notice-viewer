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

  <#-- The visualisation language is a run-time parameter passed to the the XSL transformation. -->
  <xsl:param name="language" />

  <#-- The translations compile-time parameter contains a sequence of calls to fn:doc(), which will effectivelly load all labels. -->
  <xsl:variable name="labels" select="${translations}"/>

  <#-- Number formatting settings are set by the translator at compile-time. -->
  <xsl:decimal-format decimal-separator="${decimalSeparator}" grouping-separator="${groupingSeparator}" />
  
  <#--
    The plural-label-suffix function takes an quantity (a number) as a parameter, and returns a suffix that is used to retrieve 
    the correct form (singular or plural) of the label. As the algorithm is language dependent, the function also uses the 
    $language parameter passed to the XSL transformation.
  -->
  <xsl:function name="ted:plural-label-suffix" as="xs:string">
    <xsl:param name="quantity" as="xs:decimal"/>
    <xsl:choose>
      <xsl:when test="$quantity = 1 or $quantity = -1">
        <xsl:sequence select="''"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="'.plural'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <#-- 
    The preferred-languages function returns the list of languages that should be used to retrieve labels.
    The first language is the one passed as a run-time parameter to the XSL transformation. 
    The langauges of the notice being visualised are also added to the list, in the order they are defined in the notice.
  -->
  <xsl:function name="ted:preferred-languages" as="xs:string*">
    <xsl:sequence select="($language)"/>
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

  <#-- The templates are called by the markup inserted in the body above. -->
  <#list templates as template>
    ${template}
  </#list>
</xsl:stylesheet>