<#--
    Available variables:
    - key: The key to use for rendering
    - quantity: Optional. A numeric expression used to select the plural form of the label.
    The label key is also written in the attribute data-label-key, which the page shows in debug mode.
-->
<xsl:variable name="key" select="${key}"/>
<#if quantity?has_content>
    <#--
        Look for the form of the label that matches the quantity (e.g. unit-week.few), then the general
        plural (unit-week.plural), then the singular (unit-week). If none exists, display the key.
    -->
    <xsl:variable name="plural" select="concat($key, efx:plural-label-suffix(xs:decimal(${quantity})))"/>
    <span class="label" data-label-key="{$key}"><xsl:value-of select="($labels//entry[@key=$plural]/text(), $labels//entry[@key=concat($key, '.plural')]/text(), $labels//entry[@key=$key]/text(), concat('{', $key, '}'))[1]"/></span>
<#else>
    <span class="label" data-label-key="{$key}"><xsl:value-of select="($labels//entry[@key=$key]/text(), concat('{', $key, '}'))[1]"/></span>
</#if>