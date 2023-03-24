<#--
    Available variables:
    - content: The body of the content block
    - name: Content block ID
    - number: Outline number
-->

<xsl:template name="${name}">
	<#if parameters??>
		<#list parameters as parameter>
			<xsl:param name="${parameter}" />
		</#list>
	</#if>
	<section title="${name}">
		<#if number?has_content>
		  <xsl:text>${number} </xsl:text>
		</#if>
		${content}
	</section>
</xsl:template>