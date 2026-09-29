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
	<#if quantity?has_content>
		<#-- The quantity is evaluated here, outside the loop below, where the context item is a label key. -->
		<xsl:variable name="suffix${variableSuffix}" select="efx:plural-label-suffix(xs:decimal(${quantity}))"/>
	</#if>
	<xsl:variable name="labels${variableSuffix}" as="xs:string*">
		<xsl:for-each select="${expression}">
			<#if quantity?has_content>
				<xsl:variable name="singular${variableSuffix}" select="."/>
				<xsl:variable name="plural${variableSuffix}" select="concat(., $suffix${variableSuffix})"/>
				<#--
					Look for the form of the label that matches the quantity, then the general plural, then the singular.
					If none exists, display the label key.
				-->
				<xsl:value-of select="($labels//entry[@key=$plural${variableSuffix}]/text(), $labels//entry[@key=concat($singular${variableSuffix}, '.plural')]/text(), $labels//entry[@key=$singular${variableSuffix}]/text(), concat('{', $singular${variableSuffix}, '}'))[1]"/>
			<#else>
				<xsl:variable name="label${variableSuffix}" select="."/>
				<#-- If the label does not exist, then the label key is displayed instead. -->
				<xsl:value-of select="($labels//entry[@key=$label${variableSuffix}]/text(), concat('{', $label${variableSuffix}, '}'))[1]"/>
			</#if>
		</xsl:for-each>
	</xsl:variable>

	<xsl:value-of select="string-join($labels${variableSuffix}, ', ')"/>
</span>