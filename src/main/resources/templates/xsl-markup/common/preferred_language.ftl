<#--
  The preferred-language and preferred-language-text functions take the elements of a multilingual
  field, and return, for each value of the field, the language and the text of its element in the
  first of the $PREFERRED_LANGUAGES it is available in, or else of its first element.

  The elements that share a parent are one value, in one or more languages: a multilingual field
  does not repeat within its parent. So when the elements come from several parents, for example
  the descriptions of two EU funds, one language and one text are returned for each of them.
-->
<xsl:function name="efx:preferred-language" as="xs:string*">
  <xsl:param name="ref" as="node()*"/>
  <xsl:for-each-group select="$ref" group-by="generate-id(..)">
    <xsl:sequence select="(for $language in $PREFERRED_LANGUAGES return current-group()[./@languageID=$language], current-group())[1]/@languageID"/>
  </xsl:for-each-group>
</xsl:function>

<xsl:function name="efx:preferred-language-text" as="xs:string*">
  <xsl:param name="ref" as="node()*"/>
  <xsl:for-each-group select="$ref" group-by="generate-id(..)">
    <xsl:sequence select="(for $language in $PREFERRED_LANGUAGES return current-group()[./@languageID=$language], current-group())[1]/normalize-space(text())"/>
  </xsl:for-each-group>
</xsl:function>
