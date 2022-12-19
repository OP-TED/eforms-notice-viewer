<xsl:template name='${name}'>
	<section title="${name}">
		<#if number?has_content>
		  <xsl:text>${number} </xsl:text>
		</#if>
		${content}
	</section>
</xsl:template>