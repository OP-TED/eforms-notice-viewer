<#--
    Available variables:
    - context: Context path
    - name: Content block ID
-->

<xsl:for-each select="${context}">
	<xsl:call-template name="${name}"/>
</xsl:for-each>