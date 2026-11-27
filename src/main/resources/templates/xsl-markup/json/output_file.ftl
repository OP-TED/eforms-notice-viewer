<#--
    Available variables:
    - translations: The available translations
    - globals: A list of variable and function declarations already rendered in XSLT.
    - body: The main content
    - templates: A list of XSL templates to be added to the XSLT stylesheet.
    - decimalSeparator: The decimal separator to be used in the output
    - groupingSeparator: The grouping separator to be used in the output
    - udfNamespace: The namespace to be used for user-defined functions
-->
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
  xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
  xmlns:efext="http://data.europa.eu/p27/eforms-ubl-extensions/1"
  xmlns:efac="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1"
  xmlns:efbc="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1"
  xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
  xmlns:efx="http://ted.europa.eu/efx"
  xmlns:${udfNamespace}="http://ted.europa.eu/efx/user-defined-functions" 
  exclude-result-prefixes="efx ${udfNamespace}">

  <xsl:output method="text" encoding="UTF-8"/>

  <#-- The visualisation language is a run-time parameter passed to the the XSL transformation. -->
  <xsl:param name="LANGUAGE" />

  <#-- 
    The PREFERRED_LANGUAGES variable returns the list of languages that should be used to retrieve labels.
    The first language is the one passed as a run-time parameter to the XSL transformation. 
    The langauges of the notice being visualised are also added to the list, in the order they are defined in the notice.
  -->
  <xsl:variable name="PREFERRED_LANGUAGES" select="(efx:three-letter-language-code($LANGUAGE), /*/cbc:NoticeLanguageCode, for $lang in /*/cac:AdditionalNoticeLanguage/cbc:ID return $lang)" as="xs:string*"/>

  <#-- The translations compile-time parameter contains a sequence of calls to fn:doc(), which will effectivelly load all labels. -->
  <xsl:variable name="labels" select="${translations}"/>

  <#-- What the calls at the top of a section pass on: no navigation id prefix and offsets 0 (see fragment_definition.ftl). -->
  <xsl:variable name="__navIdPrefix" select="''"/>
  <xsl:variable name="__nestingLevelOffset" select="0"/>
  <xsl:variable name="__headingLevelOffset" select="0"/>

  <#-- Insert the user-defined variable declarations -->
  <#if globals?has_content>
    <#list globals as markup>
      ${markup}
    </#list>
  </#if>

  <#-- Number formatting settings are set by the translator at compile-time. -->
  <xsl:decimal-format decimal-separator="${decimalSeparator}" grouping-separator="${groupingSeparator}" />
  
  <#-- Selects the plural form of a label from a quantity and the display language. -->
  <#include "../common/plural_label_suffix.ftl">

  <xsl:function name="efx:three-letter-language-code" as="xs:string">
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

  <xsl:function name="efx:preferred-language" as="xs:string*">
    <xsl:param name="ref" as="node()*"/>
    <xsl:sequence select="(for $language in $PREFERRED_LANGUAGES return $ref[./@languageID=$language], $ref)[1]/@languageID"/>
  </xsl:function>

  <xsl:function name="efx:preferred-language-text" as="xs:string*">
    <xsl:param name="ref" as="node()*"/>
    <xsl:sequence select="(for $language in $PREFERRED_LANGUAGES return $ref[./@languageID=$language], $ref)[1]/normalize-space(text())"/>
  </xsl:function>


  <#include "../common/section_roles.ftl">

  <#--
    The response holds the main section and the summary as HTML, and the navigation as the entries of
    a menu. It is built as the XML representation of JSON and converted with xml-to-json, so that
    every string is escaped and the result is always valid JSON.
  -->
  <#-- Each section is held in a document, also when the template has no lines in it. -->
  <xsl:template match="/">
    <xsl:variable name="body" as="document-node()">
      <xsl:document>
        <#list body as markup>
          ${markup}
        </#list>
      </xsl:document>
    </xsl:variable>

    <xsl:variable name="summary-lines" as="document-node()">
      <xsl:document>
        <#list summary as markup>
          ${markup}
        </#list>
      </xsl:document>
    </xsl:variable>
    <xsl:variable name="summary">
      <xsl:apply-templates select="$summary-lines" mode="summary"/>
    </xsl:variable>

    <xsl:variable name="navigation" as="document-node()">
      <xsl:document>
        <#list navigation as markup>
          ${markup}
        </#list>
      </xsl:document>
    </xsl:variable>

    <#-- The body and the summary are written as HTML, as the HTML page is: an empty element such as
         <span class="value"></span> stays a pair of tags, which a browser cannot take for an opening
         tag that holds the lines after it. -->
    <xsl:variable name="html" select="map{'method': 'html', 'indent': false()}"/>
    <xsl:variable name="json">
      <fn:map>
        <fn:string key="body"><xsl:value-of select="serialize($body, $html)"/></fn:string>
        <fn:string key="summary"><xsl:value-of select="serialize($summary, $html)"/></fn:string>
        <fn:array key="navigation"><xsl:apply-templates select="$navigation/section" mode="menu"/></fn:array>
      </fn:map>
    </xsl:variable>
    <xsl:value-of select="xml-to-json($json)"/>
  </xsl:template>

  <#--
    A line of the navigation that displays something itself becomes one entry of the menu: what it
    displays is the label, the link to its heading is the url if it is a heading, and the lines
    displayed in it are the entries under it. A line that displays nothing itself gives its place to
    the entries of the lines displayed in it.
  -->
  <xsl:template match="section" mode="menu">
    <xsl:variable name="label" select="normalize-space(string-join(node() except section))"/>
    <xsl:variable name="children">
      <xsl:apply-templates select="section" mode="menu"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$label">
        <fn:map>
          <fn:string key="label"><xsl:value-of select="$label"/></fn:string>
          <xsl:if test="@id">
            <fn:string key="url"><xsl:value-of select="concat('#', @id)"/></fn:string>
          </xsl:if>
          <xsl:if test="$children/*">
            <fn:array key="children"><xsl:sequence select="$children/*"/></fn:array>
          </xsl:if>
        </fn:map>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$children/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <#-- The templates are called by the markup inserted in the body above. -->
  <#list templates as template>
    ${template}
  </#list>
</xsl:stylesheet>