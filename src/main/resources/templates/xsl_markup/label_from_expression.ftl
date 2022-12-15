
<span class="dynamic-label">
	<xsl:variable name="labels${variableCounter}" as="xs:string*">
		<xsl:for-each select="${expression}">
			<xsl:variable name="label${variableCounter}" select="."/>
			<xsl:value-of select="($labels//entry[@key=$label${variableCounter}]/text(), concat('{', $label${variableCounter}, '}'))[1]"/>
		</xsl:for-each>
	</xsl:variable>
	<xsl:value-of select="string-join($labels${variableCounter}, ', ')"/>
</span>