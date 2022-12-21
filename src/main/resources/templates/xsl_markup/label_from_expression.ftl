<#--
    Available variables:
    - expression: The expression to use for rendering
    - labelSuffix: Suffix for labels
-->
<span class="dynamic-label">
	<xsl:variable name="labels${labelSuffix}" as="xs:string*">
		<xsl:for-each select="${expression}">
			<xsl:variable name="label${labelSuffix}" select="."/>
			<xsl:value-of select="($labels//entry[@key=$label${labelSuffix}]/text(), concat('{', $label${labelSuffix}, '}'))[1]"/>
		</xsl:for-each>
	</xsl:variable>
	<xsl:value-of select="string-join($labels${labelSuffix}, ', ')"/>
</span>