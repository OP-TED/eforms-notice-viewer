<#--
    Available variables:
    - name: Content block identifier
    - context: Context XPath
    - variables: Lits of additional variables (optional)
-->

<xsl:for-each select="${context}">
    <#list variables as variable>
        <xsl:variable name="${variable[0]}" select = "${variable[1]}" />
    </#list>
	<xsl:call-template name="${name}">
        <#list variables as variable>
            <xsl:with-param name="${variable[0]}" select = "$${variable[0]}" />
        </#list>
    </xsl:call-template>
</xsl:for-each>