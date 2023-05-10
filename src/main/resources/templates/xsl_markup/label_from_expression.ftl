<#--
    Available variables:
    - expression: Expression that generates the label key.
    - variableSuffix: Suffix provided for uniqueness of variable names.
	- quantity: A numeric quantity used to decide if the label needs to be pluralized.
-->
<span class="dynamic-label">
	<#-- 
		The expression may return a sequence, so we will iterate over each label returned by the expression. 
		In the process we will collect the labels into a variable.
		After we are done iterating, we will join the labels together in a comma separated list. 
	-->
	<xsl:variable name="labels${variableSuffix}" as="xs:string*">
		<xsl:for-each select="${expression}">
			<#if quantity?has_content>
				<xsl:variable name="singular${variableSuffix}" select="."/>
				<xsl:variable name="plural${variableSuffix}" select="concat(., ted:pluralSuffix(${quantity}))"/>
				<#-- This will fallback to the singual form if a pluralised label does not exist. -->
				<#-- If the singular form label does not exist either, then the label key will be shown instead. -->
				<xsl:value-of select="($labels//entry[@key=$plural${variableSuffix}]/text(), $labels//entry[@key=$singular${variableSuffix}]/text(), concat('{', $singular${variableSuffix}, '}'))[1]"/>
			<#else>
				<xsl:variable name="label${variableSuffix}" select="."/>
				<#-- If the label does not exist, then the label key is displayed instead. -->
				<xsl:value-of select="($labels//entry[@key=$label${variableSuffix}]/text(), concat('{', $label${variableSuffix}, '}'))[1]"/>
			</#if>
		</xsl:for-each>
	</xsl:variable>

	<xsl:value-of select="string-join($labels${variableSuffix}, ', ')"/>
</span>