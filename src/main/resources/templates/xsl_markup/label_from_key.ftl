<#--
    Available variables:
    - key: The key to use for rendering
-->
<span class="label"><xsl:value-of select="($labels//entry[@key=${key}]/text(), concat('{', ${key}, '}'))[1]"/></span>