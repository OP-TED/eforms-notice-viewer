<#--
    Available variables:
    - key: The key to use for rendering
    - quantity: If pluralisation is required, then this contains the suffix that identifies the plural form of the label.
-->
<xsl:variable name="key" select="${key}"/>
<#if quantity?has_content>
    <xsl:variable name="plural" select="concat($key, efx:plural-label-suffix(${quantity}))"/>
    <xsl:value-of select="($labels//entry[@key=$plural]/text(), $labels//entry[@key=$key]/text(), concat('{', $key, '}'))[1]"/>
<#else>
    <xsl:value-of select="($labels//entry[@key=$key]/text(), concat('{', $key, '}'))[1]"/>
</#if>