<#--
	FreeMarkerTemplate.FRAGMENT_DEFINITION
	Used by MarkupGenerator.renderFragmentDefinition

	Renders a fragment definition as an xsl:template.
	In EFX Template terminology, a fragment is a piece of markup that can be reused.
	Fragments are defined once and can be invoked as many times as needed.
	As a concept, a fragments is the EFX equivalent of an xsl:template in XSLT. 

    Parameters:
    - name: 		Fragment identifier. 
					The fragment will be invoked with this name when called.
	- parameters: 	A list of parameters that will be passed to the fragment when called.
					Each parameter is an object with a parameter.name and a parameter.type property.
    - number: 		Outline number. 
					Display if present.
	- conditionals: A list of conditionals, each of which is an object with a conditional.condition and a conditional.content property.
					If conditional.condition is true then conditional.content should be displayed. 
    - content: 		The content of this fragment.
					If the conditionals parameter is not empty, then this is the content to be used when no condition is et (under "otherwise").
					If no conditionals are passed, then this is the content to display for this fragment.
	- children: 	The children of this fragment  are other fragments that need to be rendered nested under this one.
-->

<xsl:template name="${name}">
	<#if parameters??>
		<#list parameters as parameter>
			<xsl:param name="${parameter.name}" />
		</#list>
	</#if>
	<xsl:text>{</xsl:text>
		<#if number?has_content && children?has_content>
		  <xsl:text>${number}&#160;</xsl:text>
		</#if>
		<#if conditionals?? && conditionals?size gt 0>
            <xsl:choose>
                <#list conditionals as item>
                    <xsl:when test="${item.condition}">
                        <xsl:text>"label": "</xsl:text>${item.content}<xsl:text>"</xsl:text>
                    </xsl:when>
                </#list>
                <xsl:otherwise>
                    <xsl:text>"label": "</xsl:text>${content}<xsl:text>"</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
		<#else>
			<xsl:text>"label": "</xsl:text>${content}<xsl:text>"</xsl:text>
        </#if>
		<#if number?has_content>
		  <xsl:text>, "url": "#section${number?replace('.', '_')}"</xsl:text>
		</#if>
		<#if children?has_content>
			<xsl:text>, "children" : [</xsl:text>
			${children}
			<xsl:text>]</xsl:text>
		</#if>
	<xsl:text>}</xsl:text>
</xsl:template>