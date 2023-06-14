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
  <xsl:param name="LANGUAGE" />

  <#-- 
    The PREFERRED_LANGUAGES variable returns the list of languages that should be used to retrieve labels.
    The first language is the one passed as a run-time parameter to the XSL transformation. 
    The langauges of the notice being visualised are also added to the list, in the order they are defined in the notice.
  -->
  <xsl:variable name="PREFERRED_LANGUAGES" select="(ted:three-letter-language-code($LANGUAGE), /*/cbc:NoticeLanguageCode, for $lang in /*/cac:AdditionalNoticeLanguage/cbc:ID return $lang)" as="xs:string*"/>

  <#-- The translations compile-time parameter contains a sequence of calls to fn:doc(), which will effectivelly load all labels. -->
  <xsl:variable name="labels" select="${translations}"/>

  <#-- Number formatting settings are set by the translator at compile-time. -->
  <xsl:decimal-format decimal-separator="${decimalSeparator}" grouping-separator="${groupingSeparator}" />
  
  <#--
    The plural-label-suffix function takes an quantity (a number) as a parameter, and returns a suffix that is used to retrieve 
    the correct form (singular or plural) of the label. As the algorithm is language dependent, the function also uses the 
    $LANGUAGE parameter passed to the XSL transformation.
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

  <xsl:function name="ted:three-letter-language-code" as="xs:string">
    <xsl:param name="two-letter-code" as="xs:string"/>
    <xsl:variable name="language-map">
      <languages>
        <language><c2>en</c2><c3>ENG</c3></language>
        <language><c2>bg</c2><c3>BUL</c3></language>
        <language><c2>cs</c2><c3>CES</c3></language>
        <language><c2>da</c2><c3>DAN</c3></language>
        <language><c2>de</c2><c3>DEU</c3></language>
        <language><c2>el</c2><c3>ELL</c3></language>
        <language><c2>et</c2><c3>EST</c3></language>
        <language><c2>fi</c2><c3>FIN</c3></language>
        <language><c2>fr</c2><c3>FRA</c3></language>
        <language><c2>ga</c2><c3>GLE</c3></language>
        <language><c2>hr</c2><c3>HRV</c3></language>
        <language><c2>hu</c2><c3>HUN</c3></language>
        <language><c2>it</c2><c3>ITA</c3></language>
        <language><c2>lv</c2><c3>LAV</c3></language>
        <language><c2>lt</c2><c3>LIT</c3></language>
        <language><c2>mt</c2><c3>MLT</c3></language>
        <language><c2>nl</c2><c3>NLD</c3></language>
        <language><c2>pl</c2><c3>POL</c3></language>
        <language><c2>pt</c2><c3>POR</c3></language>
        <language><c2>ro</c2><c3>RON</c3></language>
        <language><c2>sk</c2><c3>SLK</c3></language>
        <language><c2>sl</c2><c3>SLV</c3></language>
        <language><c2>es</c2><c3>SPA</c3></language>
        <language><c2>sv</c2><c3>SWE</c3></language>
      </languages>
    </xsl:variable>
    <xsl:sequence select="$language-map//language[c2=$two-letter-code]/c3/text()"/>
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