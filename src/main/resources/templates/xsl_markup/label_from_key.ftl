<#--
    Available variables:
    - key: The key to use for rendering
	- quantity: If pluralisation is required, then this contains the suffix that identifies the plural form of the label.
-->
<#if quantity?has_content>
    <xsl:variable name="plural" select="concat(${key}, ted:pluralSuffix(${quantity}))"/>
    <span class="label"><xsl:value-of select="($labels//entry[@key=$plural]/text(), $labels//entry[@key=${key}}]/text(), concat('{', ${key}, '}'))[1]"/></span>
<#else>
    <span class="label"><xsl:value-of select="($labels//entry[@key=${key}]/text(), concat('{', ${key}, '}'))[1]"/></span>
</#if>